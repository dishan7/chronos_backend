## Job Scheduling & Execution System

## Overview

This project implements a distributed job scheduling and execution system using **Spring Boot**, **Apache Kafka**, and a relational database. Users can create jobs with a specified execution time, and the system ensures reliable, scheduled execution with retry handling and status tracking.

The architecture follows an event-driven approach to achieve scalability, fault tolerance, and loose coupling between services.

---

## High-Level Architecture

1. Users create jobs via an API.
2. A scheduler service scans the database every minute to identify jobs scheduled for execution in the current and next one-minute window.
3. Eligible jobs are published to a Kafka topic.
4. A consumer service assigns jobs to a task scheduler for execution at the specified time.
5. Job execution results are published to a separate Kafka topic.
6. A downstream consumer updates the job status and output in the database.

---

## Job Execution Flow

1. **Job Creation**
   - Users submit jobs with execution metadata (command, execution time, etc.).
   - Jobs are persisted in the database with an initial status.

2. **Job Discovery**
   - A scheduled process scans the database every minute.
   - Jobs scheduled to run within the current and upcoming minute are selected.

3. **Job Dispatch**
   - Selected jobs are produced to the Kafka topic:  
     `scheduled-jobs-topic`

4. **Job Execution**
   - A Kafka consumer listens to the scheduler topic.
   - Jobs are assigned to a task scheduler and executed at the configured execution time.

5. **Retry Mechanism**
   - If a job execution fails, it is retried up to **3 times**.
   - Retry attempts are tracked per job.
   - After the maximum retries are exhausted, the job is marked as **FAILED**.

6. **Result Publishing**
   - Job execution results (status, output, error details) are published to:  
     `scheduled-updated-jobs-topic`

7. **Database Update**
   - A result consumer listens to the results topic.
   - Job status, execution output, and retry metadata are updated in the database.

---

## Retry Policy

- Maximum retry attempts: **3**
- Retries are triggered only on execution failure.
- Each retry is treated as a new execution attempt.
- Jobs exceeding the retry limit are marked as **FAILED**.

---

## Technology Stack

- **Backend:** Spring Boot
- **Messaging:** Apache Kafka
- **Database:** PostgreSQL (or any relational DB)
- **Scheduler:** Spring Task Scheduler

---

## Kafka Topics

| Property Key                  | Topic Name                     | Purpose                          |
|-------------------------------|--------------------------------|----------------------------------|
| `kafka.topic.jobs`            | `scheduled-jobs-topic`         | Dispatch jobs for execution      |
| `kafka.topic.updatedJobs`     | `scheduled-updated-jobs-topic` | Publish execution results        |

---

## Job Status Lifecycle
SCHEDULED → RUNNING → SUCCESS
↘
FAILED (after 3 retries)


---

## Fault Tolerance & Reliability

- Event-driven architecture using Kafka
- Retry mechanism for transient failures
- Idempotent consumers to avoid duplicate processing
- Decoupled services for scalability and resilience

---

## Running the Application

### Prerequisites

- Java 17+
- Apache Kafka
- PostgreSQL (or compatible relational database)

### Steps

1. Start Kafka and Zookeeper.
2. Start the database.
3. Configure application properties:
   ```properties
   kafka.topic.jobs=scheduled-jobs-topic
   kafka.topic.updatedJobs=scheduled-updated-jobs-topic
4. Run the Spring Boot services.

## Author

**Dishan Chutani**

