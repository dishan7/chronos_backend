package com.chronos.job_scheduler.entity;

import com.chronos.job_scheduler.enums.JOB_STATUS;
import jakarta.persistence.*;
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

    private String name;

    private String command;

    private JOB_STATUS status;

    @Column(columnDefinition = "TEXT")
    private String output;

    private LocalDateTime runAt;

    @Column(columnDefinition = "TEXT")
    private String error;

    private String path;

    private String cron_expression;

    @Column(name = "created_by_username", nullable = false)
    private String createdByUsername;
}
