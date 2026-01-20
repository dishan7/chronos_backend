package com.chronos.job_scheduler.service;

import com.chronos.job_scheduler.config.CommandValidator;
import com.chronos.job_scheduler.config.WebConfig;
import com.chronos.job_scheduler.dto.CommandResultDto;
import com.chronos.job_scheduler.dto.JobDto;
import com.chronos.job_scheduler.entity.Job;
import com.chronos.job_scheduler.enums.JOB_STATUS;
import com.chronos.job_scheduler.exception.JobNotFoundException;
import com.chronos.job_scheduler.repository.JobRepository;
import com.chronos.job_scheduler.util.TokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class JobService {

    @Autowired
    private JobRepository _jobRepository;

    @Autowired
    private CommandValidator _commandValidator;

    @Autowired
    private WebClient webClient;

    public Job fetchJobById(Long jobId){
        Job savedJob = _jobRepository.findById(jobId).orElse(null);
        if(savedJob == null) throw new JobNotFoundException("No job found with id: " + jobId);
        return savedJob;
    }

    public List<Job> fetchJobsByUser(String username){
        return _jobRepository.findJobByCreatedByUsername(username);
    }

    private boolean validateCron(String cron){
        return CronExpression.isValidExpression(cron);
    }

    public Job executeNow(JobDto jobDto, String username) throws Exception {
        Job job = new Job();
        job.setCommand(jobDto.getCommand());
        job.setCreatedByUsername(username);
        job.setName(jobDto.getName());
        job.setPath(jobDto.getPath());
        job.setOutput("");
        job.setError("");
        job.setRunAt(null);
        job.setCron_expression(null);
        job.setStatus(JOB_STATUS.RUNNING);

        Job savedJob = _jobRepository.save(job);
        String authorizationToken = TokenUtil.generateToken("executeJob", "JOB_SCHEDULER");
        webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/executeNow")
                        .build())
                .header("Authorization", "Bearer " + authorizationToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(savedJob)
                .retrieve()
                .bodyToMono(Void.class)
                .block();
        return _jobRepository.findById(savedJob.getId()).orElse(null);
    }

    public Job createJob(JobDto jobDto, String username) throws Exception {
        Job job = new Job();
        if(jobDto.getCron_expression() != null && !validateCron(jobDto.getCron_expression())){
            throw new Exception("Invalid Cron Expression");
        }
        _commandValidator.validate(jobDto.getCommand());
        job.setName(jobDto.getName());
        job.setCommand(jobDto.getCommand());
        job.setStatus(JOB_STATUS.SCHEDULED);
        job.setRunAt(jobDto.getRunAt());
        job.setOutput("");
        job.setError("");
        job.setCron_expression(jobDto.getCron_expression());
        job.setPath(jobDto.getPath());
        job.setCreatedByUsername(username);

        return _jobRepository.save(job);
    }

    public void changeJobStatus(Long jobId, String jobStatus){
        Job job = _jobRepository.findById(jobId).orElse(null);
        if(job == null) throw new JobNotFoundException("No Job Found with id: " + jobId);
        job.setStatus(JOB_STATUS.valueOf(jobStatus));
        _jobRepository.save(job);
    }

    public List<Job> fetchJobsInTimeFrame(LocalDateTime start, LocalDateTime end){
        return _jobRepository.fetchJobsInTimeFrame(start, end);
    }

    public Job updateJobDetails(Long jobId, LocalDateTime nextRunAt, CommandResultDto result){
        Job savedJob = _jobRepository.findById(jobId).orElse(null);
        if(savedJob == null) throw new JobNotFoundException("No job found with Id: " + jobId);
        savedJob.setStatus(JOB_STATUS.valueOf(result.getStatus()));
        savedJob.setOutput(result.getOutput());
        savedJob.setRunAt(nextRunAt);
        savedJob.setError(result.getError());
        return _jobRepository.save(savedJob);
    }

    public Job rescheduleJob(Long jobId, LocalDateTime runAt){
        Job savedJob = _jobRepository.findById(jobId).orElse(null);
        if(savedJob == null){
            throw new JobNotFoundException("No job found with Id: " + jobId);
        }
        savedJob.setRunAt(runAt);
        return _jobRepository.save(savedJob);
    }

    public Job cancelJob(Long jobId){
        Job savedJob = _jobRepository.findById(jobId).orElse(null);
        if(savedJob == null){
            throw new JobNotFoundException("No job found with Id: " + jobId);
        }
        savedJob.setStatus(JOB_STATUS.CANCELLED);
        return _jobRepository.save(savedJob);
    }

    @ExceptionHandler(JobNotFoundException.class)
    public ResponseEntity<String> handleJobNotFoundException(Exception e){
        return ResponseEntity.status(404).body(e.getMessage());
    }
}
