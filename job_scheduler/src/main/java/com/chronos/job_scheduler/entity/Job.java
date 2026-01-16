package com.chronos.job_scheduler.entity;

import com.chronos.job_scheduler.enums.JOB_STATUS;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Job name cannot be blank")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Command cannot be blank")
    @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    private String command;

    @NotNull(message = "Job status is required")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JOB_STATUS status;

    @Column(columnDefinition = "TEXT")
    private String output;

    private LocalDateTime runAt;

    @Column(columnDefinition = "TEXT")
    private String error;

    @NotBlank(message = "Path cannot be blank")
    @Size(max = 255)
    @Column(nullable = false)
    private String path;

    private String cron_expression;

    @NotBlank(message = "createdByUsername is required")
    @Size(max = 50)
    @Column(name = "created_by_username", nullable = false, length = 50)
    private String createdByUsername;
}
