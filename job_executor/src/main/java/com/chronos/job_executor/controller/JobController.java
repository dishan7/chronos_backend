package com.chronos.job_executor.controller;

import com.chronos.job_executor.dto.JobDto;
import com.chronos.job_executor.service.JobProducerService;
import com.chronos.job_executor.service.JobService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public Mono<Void> executeNow(@RequestBody JobDto job){
        return Mono.fromRunnable( () -> _jobProducerService.produceJobs(job));
    }

//    @GetMapping("/fetchJobsInTimeFrame")
//    public List<JobDto> fetchJobsInTimeFrame(){
//        String start = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).toString();
//        String end = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES).plusMinutes(1).toString();
//        String url = "http://localhost:8081/job/fetchJobsInTimeFrame?start=" + start + "&end=" + end;
//        System.out.println(url);
//        ResponseEntity<List<JobDto>> response =
//                _restTemplate.exchange(
//                        url,
//                        HttpMethod.GET,
//                        null,
//                        new ParameterizedTypeReference<List<JobDto>>() {}
//                );
//
//        return response.getBody();
//    }

//    @GetMapping("/fetchJobsInTimeFrame2")
//    public List<JobDto> fetchJobsInTimeFrame2(@RequestParam(name = "start") LocalDateTime start,
//                                              @RequestParam(name = "end") LocalDateTime end){
//        return _jobService.fetchJobsInTimeFrame();
//    }
}
