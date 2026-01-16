package com.chronos.job_executor.service;

import com.chronos.job_executor.dto.CommandResultDto;
import com.chronos.job_executor.dto.JobDto;
import com.chronos.job_executor.dto.JobEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;


@Service
public class JobSchedulerService {

    private final TaskScheduler _taskScheduler;

    @Autowired
    private JobService _jobService;

    @Autowired
    private JobProducerService _jobProducerService;

    private final Map<Long, ScheduledFuture<?>> scheduledJobs = new ConcurrentHashMap<>();

    public JobSchedulerService(TaskScheduler taskScheduler) {
        this._taskScheduler = taskScheduler;
    }

    public void schedule(JobDto job){
        cancel(job.getId());

        Runnable task = () -> execute(job);

        Instant executionTime = job.getRunAt()
                .atZone(ZoneId.systemDefault())
                .toInstant();

        ScheduledFuture<?> future =
                _taskScheduler.schedule(task, executionTime);

        scheduledJobs.put(job.getId(), future);

        System.out.println("Scheduled job " + job.getId()
                + " with runAt " + job.getRunAt());
    }

    private void execute(JobDto job) {
        try {
            System.out.println("execution started for job: " + job.getId());
            System.out.println(job.getCommand());
            System.out.println(job.getPath());
            System.out.println(job.getName());
            CommandResultDto result = _jobService.execute(job);
            System.out.println("updating job details now");
//            _jobService.updateJobDetails(job, result);

            //producing to kafka
            _jobProducerService.produceJobUpdate(new JobEvent(job, result));
        } catch (Exception e) {
            System.out.println("Execution failed for job " + job.getId());
        }
    }

    private void cancel(Long jobId){
        ScheduledFuture<?> future = scheduledJobs.remove(jobId);
        if (future != null) {
            future.cancel(false);
        }
    }
}
