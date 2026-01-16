package com.chronos.job_executor.controller;

import com.chronos.job_executor.dto.JobDto;
import com.chronos.job_executor.service.JobProducerService;
import com.chronos.job_executor.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import reactor.core.publisher.Mono;

@RestController
public class JobController {

    @Autowired
    private RestTemplate _restTemplate;

    @Autowired
    private JobService _jobService;

    @Autowired
    private JobProducerService _jobProducerService;

    @PutMapping("/executeNow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('JOB_SCHEDULER')")
    public Mono<Void> executeNow(@RequestBody JobDto job) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        authentication.getAuthorities().forEach(authority -> {
            System.out.println(authority.getAuthority());
        });
        if(!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JOB_SCHEDULER"))){
            throw new Exception("Unauthorized request");
        }
        return Mono.fromRunnable( () -> _jobProducerService.produceJobs(job));
    }
}
