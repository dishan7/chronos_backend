package com.chronos.job_executor.dto;

import com.chronos.job_executor.enums.JOB_STATUS;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class JobDto {

    private Long id;

    private String name;

    private String command;

    private JOB_STATUS status;

    private String output;

    private LocalDateTime runAt;

    private String path;

    private String cron_expression;

    private String createdByUsername;
}
