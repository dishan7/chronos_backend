package com.chronos.job_scheduler.controller;

import com.chronos.job_scheduler.dto.CommandResultDto;
import com.chronos.job_scheduler.dto.JobDto;
import com.chronos.job_scheduler.entity.Job;
import com.chronos.job_scheduler.service.JobService;
import com.chronos.job_scheduler.service.SecurityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/job")
public class JobController {

    @Autowired
    private JobService _jobService;

    @Autowired
    private SecurityService _securityService;

    @GetMapping("/fetchJobById")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Job> fetchJobById(@RequestParam(name = "jobId") Long jobId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        if(!_securityService.isOwner(username, jobId)) throw new RuntimeException("Unauthorized request!");
        Job savedJob =  _jobService.fetchJobById(jobId);
        return ResponseEntity.status(200).body(savedJob);
    }

    @GetMapping("/fetchJobsByUser")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Job>> fetchJobsByUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        List<Job> jobsByUser = _jobService.fetchJobsByUser(username);
        return ResponseEntity.status(200).body(jobsByUser);
    }

    @PostMapping("/executeNow")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Job> executeNow(@RequestBody @Valid JobDto jobDto) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Job createdJob = _jobService.executeNow(jobDto, username);
        return ResponseEntity.status(200).body(createdJob);
    }

    @PostMapping("/createJob")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Job> createJob(@RequestBody @Valid JobDto jobDto) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        Job createdJob = _jobService.createJob(jobDto, username);
        return ResponseEntity.status(200).body(createdJob);
    }

    @PutMapping("/changeJobStatus")
    public ResponseEntity<String> changeJobStatus(@RequestParam(name = "jobId") Long jobId,
                                                  @RequestParam(name = "jobStatus") String jobStatus){
        _jobService.changeJobStatus(jobId, jobStatus);
        return ResponseEntity.status(200).body("Status changes successfully");
    }

    @GetMapping("/fetchJobsInTimeFrame")
    @PreAuthorize("hasRole('JOB_EXECUTOR')")
    public ResponseEntity<List<Job>> fetchJobsInTimeFrame(@RequestParam(name = "start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
                                                          @RequestParam(name = "end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        authentication.getAuthorities().forEach(authority -> {
            System.out.println(authority.getAuthority());
        });
        if(!authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_JOB_EXECUTOR"))){
            throw new Exception("Unauthorized request");
        }
        List<Job> jobs = _jobService.fetchJobsInTimeFrame(start, end);
        return ResponseEntity.status(200).body(jobs);
    }

    @PutMapping("/updateJobDetails")
    @PreAuthorize("hasRole('JOB_EXECUTOR')")
    public ResponseEntity<Job> updateJobDetails(@RequestParam(name = "jobId") Long jobId,
                                                @RequestParam(name = "nextRunAt") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime nextRunAt,
                                                @RequestBody CommandResultDto result){
        Job job = _jobService.updateJobDetails(jobId, nextRunAt, result);
        return ResponseEntity.status(200).body(job);
    }

    @PutMapping("/rescheduleJob")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Job> rescheduleJob(@RequestParam(name = "jobId") Long jobId,
                                         @RequestParam(name = "runAt") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime runAt){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        if(!_securityService.isOwner(username, jobId)){
            throw new RuntimeException("Unauthorized request!");
        }
        Job savedJob = _jobService.rescheduleJob(jobId, runAt);
        return ResponseEntity.status(200).body(savedJob);
    }

    @PutMapping("/cancelJob")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Job> cancelJob(@RequestParam(name = "jobId") Long jobId){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        if(!_securityService.isOwner(username, jobId)){
            throw new RuntimeException("Unauthorized request!");
        }
        Job savedJob = _jobService.cancelJob(jobId);
        return ResponseEntity.status(200).body(savedJob);
    }
}
