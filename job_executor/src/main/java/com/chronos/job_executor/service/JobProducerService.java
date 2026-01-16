package com.chronos.job_executor.service;

import com.chronos.job_executor.dto.CommandResultDto;
import com.chronos.job_executor.dto.JobDto;
import com.chronos.job_executor.dto.JobEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class JobProducerService {

    private final KafkaTemplate<String,JobDto> _kafkaTemplate;
    private final KafkaTemplate<String,JobEvent> _kafkaTemplateJobUpdate;

    @Value("${kafka.topic.jobs}")
    private String topic;

    @Value("${kafka.topic.updatedJobs}")
    private String topicJobUpdate;

    public void produceJobs(JobDto job){
        System.out.println("producing jobs to kafka: " + job.getId());
        _kafkaTemplate.send(
                topic,
                job.getId().toString(),
                job
        );
    }

    public void produceJobUpdate(JobEvent jobEvent){
        System.out.println("producing result update to kafka: " + jobEvent.getJob().getId());
        _kafkaTemplateJobUpdate.send(
                topicJobUpdate,
                jobEvent.getJob().getId().toString(),
                jobEvent
        );
    }
}
