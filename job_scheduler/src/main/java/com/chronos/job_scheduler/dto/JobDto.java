package com.chronos.job_scheduler.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Job name is required")
    @Size(max = 100, message = "Job name must be at most 100 characters")
    private String name;

    @NotBlank(message = "Command is required")
    @Size(max = 1000, message = "Command must be at most 1000 characters")
    private String command;

    private LocalDateTime runAt;

    @NotBlank(message = "Path is required")
    @Size(max = 255, message = "Path too long")
    private String path;

    private String cron_expression;
}
