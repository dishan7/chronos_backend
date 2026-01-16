package com.chronos.job_scheduler.service;

import com.chronos.job_scheduler.entity.Job;
import com.chronos.job_scheduler.exception.JobNotFoundException;
import com.chronos.job_scheduler.repository.JobRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SecurityService {

    @Autowired
    private JobRepository _jobRepository;

    public boolean isOwner(String username, Long jobId){
        Job savedJob = _jobRepository.findById(jobId).orElse(null);
        if(savedJob == null) throw new JobNotFoundException("No job found with id: " + jobId);
        return savedJob.getCreatedByUsername().equals(username);
    }
}
