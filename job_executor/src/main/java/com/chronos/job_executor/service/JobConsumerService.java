package com.chronos.job_executor.service;

import com.chronos.job_executor.dto.CommandResultDto;
import com.chronos.job_executor.dto.JobDto;
import com.chronos.job_executor.dto.JobEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class JobConsumerService {

    @Autowired
    private JobSchedulerService _jobSchedulerService;

    @Autowired
    private JobService _jobService;

    @KafkaListener(
            topics = "${kafka.topic.jobs}",
            groupId = "job-executor"
    )
    public JobDto consumeJob(JobDto job) throws Exception {
        try{
            System.out.println("consumer side: " + job.getId());
            log.info("consumer side: {}" , job.getId());
            if(job.getRunAt() == null){
                job.setRunAt(LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES));
            }
            _jobSchedulerService.schedule(job);
            return job;
        }
        catch(Exception e){
            System.out.println("error: " + e.getMessage());
            log.error("error: {}" , e.getMessage());
            throw new Exception(e.getMessage());
        }
    }

    @KafkaListener(
            topics = "${kafka.topic.updatedJobs}",
            groupId = "job-executor"
    )
    public JobEvent consumeJobUpdate(JobEvent jobEvent) throws Exception{
        try{
            System.out.println("consuming job for update: " + jobEvent.getJob().getId());
            log.info("consuming job for update: {}" , jobEvent.getJob().getId());
            _jobService.updateJobDetails(jobEvent.getJob(), jobEvent.getResult());
            return jobEvent;
        }
        catch (Exception e){
            System.out.println("error is consuming job update: " + e.getMessage());
            log.error("error is consuming job update: {}" , e.getMessage());
            throw new Exception(e.getMessage());
        }
    }
}
