package com.chronos.job_executor.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobEvent {

    private JobDto job;

    private CommandResultDto result;
}
