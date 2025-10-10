# Batch Processor API

The Batch Processor handles scheduled jobs, data maintenance, reporting, and automated business processes for the Modern Reservation System.

## 🎯 Overview

| Property | Value |
|----------|-------|
| **Service Name** | batch-processor |
| **Port** | 8085 |
| **Health Check** | `/actuator/health` |
| **API Base URL** | `http://localhost:8085/api/v1/batch` |
| **OpenAPI Spec** | `/v3/api-docs` |
| **Swagger UI** | `/swagger-ui.html` |

## 🚀 Quick Start

### Start the Service
```bash
# Via dev script
./dev.sh start batch-processor

# Via Maven
cd apps/backend/java-services/business-services/batch-processor
mvn spring-boot:run
```

### Health Check
```bash
curl http://localhost:8085/actuator/health
```

## ⚙️ Core Features

### Scheduled Jobs
- **Daily Reports** - Revenue, occupancy, and performance analytics
- **Maintenance Tasks** - Data cleanup, archival, and optimization
- **Business Rules** - Rate updates, inventory adjustments, loyalty processing
- **External Integrations** - Third-party system synchronization

### Data Processing
- **ETL Operations** - Extract, transform, and load data workflows
- **Data Validation** - Consistency checks and quality assurance
- **Archival Management** - Historical data retention and cleanup
- **Performance Optimization** - Index maintenance and query optimization

### Automated Operations
- **Reservation Workflows** - Auto-confirmations, reminders, and cancellations
- **Financial Processing** - Payment settlements, refunds, and reconciliation
- **Inventory Management** - Room status updates and availability adjustments
- **Communication** - Email campaigns, notifications, and alerts

## 🔌 API Endpoints

### List All Jobs
```http
GET /api/v1/batch/jobs
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
```

**Response:**
```json
{
  "jobs": [
    {
      "jobId": "daily-revenue-report",
      "name": "Daily Revenue Report",
      "description": "Generate daily revenue and occupancy reports",
      "schedule": "0 6 * * *",
      "status": "ACTIVE",
      "lastRun": "2025-10-10T06:00:00Z",
      "nextRun": "2025-10-11T06:00:00Z",
      "executionTime": "45s",
      "successRate": 99.2
    },
    {
      "jobId": "data-cleanup",
      "name": "Data Cleanup",
      "description": "Archive old records and optimize database",
      "schedule": "0 2 * * 0",
      "status": "ACTIVE",
      "lastRun": "2025-10-06T02:00:00Z",
      "nextRun": "2025-10-13T02:00:00Z",
      "executionTime": "2m 15s",
      "successRate": 100.0
    },
    {
      "jobId": "rate-sync",
      "name": "Rate Synchronization",
      "description": "Sync rates with external channel managers",
      "schedule": "0 */4 * * *",
      "status": "ACTIVE",
      "lastRun": "2025-10-10T12:00:00Z",
      "nextRun": "2025-10-10T16:00:00Z",
      "executionTime": "1m 30s",
      "successRate": 98.5
    }
  ],
  "summary": {
    "totalJobs": 15,
    "activeJobs": 12,
    "pausedJobs": 2,
    "failedJobs": 1,
    "avgExecutionTime": "1m 25s"
  }
}
```

### Get Job Details
```http
GET /api/v1/batch/jobs/{jobId}
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
```

**Response:**
```json
{
  "jobId": "daily-revenue-report",
  "name": "Daily Revenue Report",
  "description": "Generate comprehensive daily revenue and occupancy reports for all properties",
  "category": "REPORTING",
  "schedule": {
    "cron": "0 6 * * *",
    "timezone": "America/New_York",
    "description": "Daily at 6:00 AM EST"
  },
  "configuration": {
    "properties": ["ALL"],
    "reportTypes": ["REVENUE", "OCCUPANCY", "ADR", "REVPAR"],
    "outputFormat": "PDF",
    "emailRecipients": [
      "revenue@hotel.com",
      "management@hotel.com"
    ],
    "archiveLocation": "s3://reports/daily/"
  },
  "status": "ACTIVE",
  "execution": {
    "lastRun": "2025-10-10T06:00:00Z",
    "nextRun": "2025-10-11T06:00:00Z",
    "avgExecutionTime": "45s",
    "successRate": 99.2,
    "totalRuns": 365,
    "successfulRuns": 362,
    "failedRuns": 3
  },
  "dependencies": [
    "reservation-engine",
    "payment-processor",
    "analytics-engine"
  ],
  "monitoring": {
    "alertsEnabled": true,
    "alertThreshold": "2m",
    "notificationChannels": ["email", "slack"]
  }
}
```

### Trigger Job Manually
```http
POST /api/v1/batch/jobs/{jobId}/execute
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
Content-Type: application/json
```

**Request Body:**
```json
{
  "parameters": {
    "date": "2025-10-10",
    "properties": ["prop-123", "prop-456"],
    "reportType": "DETAILED"
  },
  "priority": "HIGH",
  "triggeredBy": "admin-user",
  "reason": "Manual execution for specific date range"
}
```

**Response:**
```json
{
  "executionId": "exec-789012",
  "jobId": "daily-revenue-report",
  "status": "RUNNING",
  "startTime": "2025-10-10T14:30:00Z",
  "estimatedCompletion": "2025-10-10T14:31:00Z",
  "parameters": {
    "date": "2025-10-10",
    "properties": ["prop-123", "prop-456"],
    "reportType": "DETAILED"
  },
  "progress": {
    "percentage": 0,
    "currentStep": "Initializing",
    "totalSteps": 5
  }
}
```

### Get Job Execution Status
```http
GET /api/v1/batch/executions/{executionId}
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
```

**Response:**
```json
{
  "executionId": "exec-789012",
  "jobId": "daily-revenue-report",
  "status": "COMPLETED",
  "startTime": "2025-10-10T14:30:00Z",
  "endTime": "2025-10-10T14:30:47Z",
  "duration": "47s",
  "progress": {
    "percentage": 100,
    "currentStep": "Completed",
    "totalSteps": 5,
    "stepsCompleted": [
      {
        "step": "Data Collection",
        "duration": "15s",
        "status": "SUCCESS",
        "recordsProcessed": 1250
      },
      {
        "step": "Revenue Calculation",
        "duration": "20s",
        "status": "SUCCESS",
        "calculations": 850
      },
      {
        "step": "Report Generation",
        "duration": "10s",
        "status": "SUCCESS",
        "pages": 12
      },
      {
        "step": "Email Distribution",
        "duration": "2s",
        "status": "SUCCESS",
        "emailsSent": 5
      }
    ]
  },
  "output": {
    "reportUrl": "https://reports.example.com/daily-revenue-2025-10-10.pdf",
    "recordsProcessed": 1250,
    "outputSize": "2.3 MB",
    "checksum": "sha256:abc123..."
  },
  "logs": {
    "logLevel": "INFO",
    "logUrl": "https://logs.example.com/exec-789012.log",
    "errorCount": 0,
    "warningCount": 2
  }
}
```

### Schedule New Job
```http
POST /api/v1/batch/jobs
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
Content-Type: application/json
```

**Request Body:**
```json
{
  "name": "Weekly Guest Satisfaction Survey",
  "description": "Send satisfaction surveys to guests who checked out in the past week",
  "category": "COMMUNICATION",
  "schedule": {
    "cron": "0 10 * * 1",
    "timezone": "America/New_York"
  },
  "configuration": {
    "surveyTemplate": "post-stay-satisfaction",
    "targetAudience": "CHECKED_OUT_LAST_7_DAYS",
    "emailTemplate": "survey-invitation",
    "excludeRecent": true,
    "maxRecipients": 1000
  },
  "dependencies": [
    "guest-service"
  ],
  "monitoring": {
    "alertsEnabled": true,
    "alertThreshold": "5m",
    "notificationChannels": ["email"]
  },
  "retryPolicy": {
    "maxRetries": 3,
    "retryDelay": "5m",
    "backoffMultiplier": 2
  }
}
```

**Response:**
```json
{
  "jobId": "weekly-guest-survey",
  "name": "Weekly Guest Satisfaction Survey",
  "status": "ACTIVE",
  "schedule": {
    "cron": "0 10 * * 1",
    "nextRun": "2025-10-14T10:00:00Z"
  },
  "createdAt": "2025-10-10T15:00:00Z",
  "createdBy": "admin-user"
}
```

### Pause/Resume Job
```http
PUT /api/v1/batch/jobs/{jobId}/status
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
Content-Type: application/json
```

**Request Body:**
```json
{
  "status": "PAUSED",
  "reason": "Maintenance window",
  "pausedBy": "admin-user"
}
```

**Response:**
```json
{
  "jobId": "daily-revenue-report",
  "previousStatus": "ACTIVE",
  "currentStatus": "PAUSED",
  "changedAt": "2025-10-10T15:30:00Z",
  "changedBy": "admin-user",
  "reason": "Maintenance window",
  "nextScheduledRun": null
}
```

### Get Job Execution History
```http
GET /api/v1/batch/jobs/{jobId}/executions?limit=50&offset=0
Authorization: Bearer <jwt-token>
X-Tenant-ID: <tenant-id>
```

**Response:**
```json
{
  "executions": [
    {
      "executionId": "exec-789012",
      "startTime": "2025-10-10T06:00:00Z",
      "endTime": "2025-10-10T06:00:45Z",
      "duration": "45s",
      "status": "COMPLETED",
      "recordsProcessed": 1250,
      "triggeredBy": "SCHEDULER"
    },
    {
      "executionId": "exec-789011",
      "startTime": "2025-10-09T06:00:00Z",
      "endTime": "2025-10-09T06:00:42Z",
      "duration": "42s",
      "status": "COMPLETED",
      "recordsProcessed": 1180,
      "triggeredBy": "SCHEDULER"
    },
    {
      "executionId": "exec-789010",
      "startTime": "2025-10-08T06:00:00Z",
      "endTime": null,
      "duration": null,
      "status": "FAILED",
      "error": "Database connection timeout",
      "triggeredBy": "SCHEDULER"
    }
  ],
  "pagination": {
    "limit": 50,
    "offset": 0,
    "total": 365,
    "hasMore": true
  },
  "summary": {
    "successRate": 99.2,
    "avgDuration": "43s",
    "totalExecutions": 365,
    "recentFailures": 1
  }
}
```

## 📊 Predefined Job Categories

### Reporting Jobs
```yaml
daily-revenue-report:
  schedule: "0 6 * * *"
  description: "Daily revenue and occupancy analysis"

weekly-performance-summary:
  schedule: "0 7 * * 1"
  description: "Weekly KPI and performance metrics"

monthly-financial-report:
  schedule: "0 8 1 * *"
  description: "Monthly P&L and financial analysis"

guest-satisfaction-analytics:
  schedule: "0 9 * * 1"
  description: "Weekly guest feedback and satisfaction trends"
```

### Maintenance Jobs
```yaml
data-cleanup:
  schedule: "0 2 * * 0"
  description: "Archive old data and optimize database"

log-rotation:
  schedule: "0 3 * * *"
  description: "Rotate and compress application logs"

cache-refresh:
  schedule: "0 4 * * *"
  description: "Refresh cached data and clear expired entries"

health-check-aggregation:
  schedule: "*/15 * * * *"
  description: "Aggregate system health metrics"
```

### Business Process Jobs
```yaml
rate-sync:
  schedule: "0 */4 * * *"
  description: "Synchronize rates with channel managers"

inventory-update:
  schedule: "0 */2 * * *"
  description: "Update room inventory across all channels"

loyalty-points-processing:
  schedule: "0 23 * * *"
  description: "Process loyalty points and tier updates"

automated-confirmations:
  schedule: "*/30 * * * *"
  description: "Send automated booking confirmations"
```

### Communication Jobs
```yaml
reminder-emails:
  schedule: "0 8 * * *"
  description: "Send check-in reminders to arriving guests"

feedback-requests:
  schedule: "0 10 * * *"
  description: "Send feedback requests to departed guests"

promotional-campaigns:
  schedule: "0 12 * * *"
  description: "Send targeted promotional emails"

birthday-greetings:
  schedule: "0 9 * * *"
  description: "Send birthday greetings to loyalty members"
```

## 🔧 Configuration

### Application Properties
```yaml
# Batch Processor Configuration
batch-processor:
  scheduler:
    pool-size: 10
    max-pool-size: 20
    queue-capacity: 100

  job-execution:
    timeout: 1800s  # 30 minutes
    retry:
      max-attempts: 3
      delay: 300s   # 5 minutes
      backoff-multiplier: 2

  monitoring:
    metrics-enabled: true
    health-check-interval: 60s
    alert-threshold: 300s  # 5 minutes

  storage:
    output-location: "s3://batch-outputs/"
    log-location: "s3://batch-logs/"
    retention-days: 90

  notifications:
    email-enabled: true
    slack-enabled: true
    webhook-enabled: false

# Database Configuration
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/batch_jobs
    username: batch_user
    password: ${BATCH_DB_PASSWORD}

  quartz:
    job-store-type: jdbc
    jdbc:
      initialize-schema: embedded
    properties:
      org:
        quartz:
          jobStore:
            driverDelegateClass: org.quartz.impl.jdbcjobstore.PostgreSQLDelegate
            useProperties: false
            misfireThreshold: 60000
```

### Environment Variables
```bash
# Database
BATCH_DB_PASSWORD=secure_batch_password

# External Services
RESERVATION_ENGINE_URL=http://localhost:8084
PAYMENT_PROCESSOR_URL=http://localhost:8082
ANALYTICS_ENGINE_URL=http://localhost:8086

# Storage
AWS_ACCESS_KEY_ID=your_access_key
AWS_SECRET_ACCESS_KEY=your_secret_key
AWS_REGION=us-east-1
S3_BUCKET_OUTPUTS=batch-outputs
S3_BUCKET_LOGS=batch-logs

# Notifications
SMTP_HOST=smtp.example.com
SMTP_PORT=587
SMTP_USERNAME=batch@hotel.com
SMTP_PASSWORD=email_password

SLACK_WEBHOOK_URL=https://hooks.slack.com/services/...

# Monitoring
OTEL_SERVICE_NAME=batch-processor
OTEL_EXPORTER_OTLP_ENDPOINT=http://localhost:4317
```

## 📈 Performance & Monitoring

### Performance Metrics
- **Job Execution Time**: <5 minutes (95th percentile)
- **Scheduler Accuracy**: <30 seconds variance
- **Success Rate**: >99% for critical jobs
- **Resource Usage**: <2GB memory, <50% CPU
- **Concurrent Jobs**: Up to 10 simultaneous executions

### Business Metrics
```
# Job execution metrics
batch_jobs_executed_total{job="daily-revenue-report",status="success"}
batch_jobs_duration_seconds{job="data-cleanup"}
batch_jobs_records_processed_total{job="rate-sync"}

# System metrics
batch_scheduler_active_jobs
batch_executor_queue_size
batch_executor_thread_pool_active

# Business impact metrics
batch_reports_generated_total
batch_data_processed_bytes
batch_notifications_sent_total
```

## 🧪 Testing

### Unit Tests
```bash
cd apps/backend/java-services/business-services/batch-processor
mvn test
```

### Integration Tests
```bash
mvn verify -P integration-tests
```

### Job Testing
```bash
# Test job execution
curl -X POST http://localhost:8085/api/v1/batch/jobs/test-job/execute \
  -H "Authorization: Bearer $TOKEN" \
  -H "X-Tenant-ID: test-tenant" \
  -H "Content-Type: application/json" \
  -d '{"parameters": {"dryRun": true}}'

# Test scheduler
curl -X GET http://localhost:8085/api/v1/batch/scheduler/status \
  -H "Authorization: Bearer $TOKEN"

# Test job configuration
curl -X POST http://localhost:8085/api/v1/batch/jobs/validate \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Job",
    "schedule": {"cron": "0 12 * * *"},
    "configuration": {"testParam": "value"}
  }'
```

## 🛠️ Development

### Local Development
```bash
# Run with development profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Run with debug
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5010"
```

### Custom Job Development
```java
@Component
public class CustomReportJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap dataMap = context.getJobDetail().getJobDataMap();
        String tenantId = dataMap.getString("tenantId");

        try {
            // Job implementation
            generateCustomReport(tenantId);

        } catch (Exception e) {
            throw new JobExecutionException("Job execution failed", e);
        }
    }

    private void generateCustomReport(String tenantId) {
        // Custom logic here
    }
}

@Configuration
public class JobConfiguration {

    @Bean
    public JobDetail customReportJobDetail() {
        return JobBuilder.newJob(CustomReportJob.class)
                .withIdentity("customReportJob")
                .withDescription("Generate custom reports")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger customReportTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(customReportJobDetail())
                .withIdentity("customReportTrigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 8 * * *"))
                .build();
    }
}
```

### Database Schema
```sql
-- Job executions table
CREATE TABLE job_executions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id VARCHAR(100) NOT NULL,
    execution_id VARCHAR(100) UNIQUE NOT NULL,
    tenant_id VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration_seconds INTEGER,
    records_processed INTEGER,
    output_location VARCHAR(500),
    error_message TEXT,
    triggered_by VARCHAR(50),
    parameters JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Job configuration table
CREATE TABLE job_configurations (
    job_id VARCHAR(100) PRIMARY KEY,
    tenant_id VARCHAR(50) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(50),
    cron_expression VARCHAR(100),
    timezone VARCHAR(50),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    configuration JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Job dependencies table
CREATE TABLE job_dependencies (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    job_id VARCHAR(100) REFERENCES job_configurations(job_id),
    depends_on_service VARCHAR(100) NOT NULL,
    dependency_type VARCHAR(50) DEFAULT 'SERVICE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

## 🚨 Troubleshooting

### Common Issues

**Job Execution Failures**
```bash
# Check job status
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8085/api/v1/batch/jobs/failed-job

# View execution logs
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8085/api/v1/batch/executions/exec-123/logs

# Retry failed execution
curl -X POST -H "Authorization: Bearer $TOKEN" \
     http://localhost:8085/api/v1/batch/executions/exec-123/retry
```

**Scheduler Issues**
```bash
# Check scheduler health
curl http://localhost:8085/actuator/health/scheduler

# View active jobs
curl -H "Authorization: Bearer $TOKEN" \
     http://localhost:8085/api/v1/batch/scheduler/active-jobs

# Restart scheduler
curl -X POST -H "Authorization: Bearer $ADMIN_TOKEN" \
     http://localhost:8085/api/v1/admin/scheduler/restart
```

**Performance Problems**
```bash
# Check thread pool status
curl http://localhost:8085/actuator/metrics/executor.pool.size

# Monitor job queue
curl http://localhost:8085/actuator/metrics/executor.queue.remaining

# View slow executions
curl -H "Authorization: Bearer $TOKEN" \
     "http://localhost:8085/api/v1/batch/executions?status=RUNNING&duration=>300"
```

---

## 📚 Related Documentation
- [Business Services Overview](../index.md)
- [Analytics Engine](../analytics-engine/)
- [Job Scheduling Best Practices](../../../guides/job-scheduling.md)
- [Data Processing Guidelines](../../../guides/data-processing.md)
