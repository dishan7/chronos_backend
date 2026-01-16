package com.chronos.job_scheduler.repository;

import com.chronos.job_scheduler.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    @Query("""
            SELECT j FROM Job j
            WHERE j.runAt BETWEEN :start AND :end
            AND j.status = com.chronos.job_scheduler.enums.JOB_STATUS.SCHEDULED
            """)
    public List<Job> fetchJobsInTimeFrame(@Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end);

    public List<Job> findJobByCreatedByUsername(String username);
}
