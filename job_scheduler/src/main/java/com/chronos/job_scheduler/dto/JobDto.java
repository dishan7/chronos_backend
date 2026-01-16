package com.chronos.job_scheduler.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobDto {

    private String name;

    private String command;

    private String output;

    private LocalDateTime runAt;

    private String path;

    private String cron_expression;
}
