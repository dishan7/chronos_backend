package com.chronos.job_executor.dto;

import com.chronos.job_executor.enums.JOB_STATUS;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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

    @NotNull(message = "Job id is required")
    private Long id;

    @NotBlank(message = "Job name is required")
    @Size(max = 100, message = "Job name must be at most 100 characters")
    private String name;

    @NotBlank(message = "Command is required")
    @Size(max = 1000, message = "Command must be at most 1000 characters")
    private String command;

    @NotNull(message = "Job status is required")
    private JOB_STATUS status;

    private String output;

    private LocalDateTime runAt;

    @NotBlank(message = "Path is required")
    @Size(max = 255, message = "Path too long")
    private String path;

    private String cron_expression;

    @NotBlank(message = "createdByUsername is required")
    @Size(max = 50, message = "createdByUsername too long")
    private String createdByUsername;
}
