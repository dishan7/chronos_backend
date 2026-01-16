package com.chronos.job_executor.service;

import com.chronos.job_executor.dto.CommandResultDto;
import com.chronos.job_executor.dto.JobDto;
import com.chronos.job_executor.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static io.netty.util.internal.PlatformDependent.isWindows;

@Service
public class JobService {

    @Autowired
    private WebClient webClient;

    @Autowired
    private JobProducerService _JobProducerService;

    public List<JobDto> fetchJobsInTimeFrame(){
        String start = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).toString();
        String end = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(1).toString();
        System.out.println(start + " - " + end);
        System.out.println("fetching jobs for next 1 min");
        String authorizationToken = TokenUtil.generateToken("fetchJobs", "JOB_EXECUTOR");
        List<JobDto> jobs =  webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/job/fetchJobsInTimeFrame")
                        .queryParam("start", start)
                        .queryParam("end", end)
                        .build())
                .header("Authorization", "Bearer " + authorizationToken)
                .retrieve()
                .bodyToFlux(JobDto.class)
                .collectList()
                .block();
        System.out.println(jobs);
        return jobs;
    }

    @Scheduled(cron = "0 */1 * * * *", zone = "Asia/Kolkata")
    public void produceJobsToKafka(){
        System.out.println("producing jobs for next 1 min");
        List<JobDto> jobs = fetchJobsInTimeFrame();

        for(JobDto job: jobs){
            System.out.println("producer side: " + job.getId());
            _JobProducerService.produceJobs(job);
        }
    }

    @Retryable(
            retryFor = {
                    IOException.class,
                    InterruptedException.class,
                    RuntimeException.class
            },
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public CommandResultDto execute(JobDto job) throws IOException, InterruptedException {
        ProcessBuilder processBuilder;
        if(isWindows()){
            processBuilder = new ProcessBuilder("cmd.exe", "/c", job.getCommand());
        }
        else{
            processBuilder = new ProcessBuilder("bash", "-c", job.getCommand());
        }
        File dir = new File(job.getPath());
        if (!dir.exists()) {
            throw new IllegalArgumentException("Directory does not exist");
        }
        processBuilder.directory(dir);
        Process process = processBuilder.start();
        String output = new String(
                process.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );
        String error = new String(
                process.getErrorStream().readAllBytes(),
                StandardCharsets.UTF_8
        );
        boolean finished = process.waitFor(60, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new RuntimeException("Command timed out");
        }
        int exitCode = process.exitValue();
        System.out.println(output);
        System.out.println(error);
        System.out.println(exitCode);
        String status = exitCode==0?"SUCCESSFUL":"FAILED";
        if(exitCode != 0){
            throw new RuntimeException("Command execution failed with exit code: " + exitCode);
        }
        if(job.getCron_expression() != null) status = "SCHEDULED";
        return new CommandResultDto(
                status,
                output,
                error
        );
    }

    @Recover
    public CommandResultDto recover(Exception e, JobDto job){
        String status = "FAILED";
        if(job.getCron_expression() != null) status = "SCHEDULED";
        return new CommandResultDto(
                status,
                "",
                "Command failed after retries: " + e.getMessage()
        );
    }

//    @Retryable(
//            retryFor = {
//                    IOException.class,
//                    InterruptedException.class,
//                    RuntimeException.class
//            },
//            maxAttempts = 3,
//            backoff = @Backoff(delay = 2000, multiplier = 2)
//    )
//    public CommandResultDto executeNow(JobDto job) throws IOException, InterruptedException {
//        ProcessBuilder processBuilder;
//        if(isWindows()){
//            processBuilder = new ProcessBuilder("cmd.exe", "/c", job.getCommand());
//        }
//        else{
//            processBuilder = new ProcessBuilder("bash", "-c", job.getCommand());
//        }
//        File dir = new File(job.getPath());
//        if (!dir.exists()) {
//            throw new IllegalArgumentException("Directory does not exist");
//        }
//        processBuilder.directory(dir);
//        Process process = processBuilder.start();
//        String output = new String(
//                process.getInputStream().readAllBytes(),
//                StandardCharsets.UTF_8
//        );
//        String error = new String(
//                process.getErrorStream().readAllBytes(),
//                StandardCharsets.UTF_8
//        );
//        boolean finished = process.waitFor(60, TimeUnit.SECONDS);
//        if (!finished) {
//            process.destroyForcibly();
//            throw new RuntimeException("Command timed out");
//        }
//        int exitCode = process.exitValue();
//        System.out.println(output);
//        System.out.println(error);
//        System.out.println(exitCode);
//        String status = exitCode==0?"SUCCESSFUL":"FAILED";
//        return new CommandResultDto(
//                status,
//                output,
//                error
//        );
//    }

    public void updateJobDetails(JobDto job, CommandResultDto result){
        String authorizationToken = TokenUtil.generateToken("fetchJobs", "JOB_EXECUTOR");
        LocalDateTime nextRunAt;
        if(job.getCron_expression() != null){
            CronExpression cron = CronExpression.parse(job.getCron_expression());
            nextRunAt = cron.next(job.getRunAt());
        } else {
            nextRunAt = job.getRunAt();
        }
        System.out.println("nextRunningTime: " + nextRunAt);

        webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/job/updateJobDetails")
                        .queryParam("jobId", job.getId())
                        .queryParam("nextRunAt", nextRunAt)
                        .build())
                .header("Authorization", "Bearer " + authorizationToken)
                .bodyValue(result)
                .retrieve()
                .bodyToMono(JobDto.class)
                .block();
    }
}
