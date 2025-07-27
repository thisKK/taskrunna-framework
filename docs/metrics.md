---
layout: default
title: Metrics & Monitoring
permalink: /metrics/
---

# 📊 Metrics & Monitoring

TaskRunna provides comprehensive Prometheus metrics out of the box for production observability.

## 🚀 Quick Start

### Enable Metrics

```kotlin
import com.taskrunna.batch.metrics.PrometheusConfig

// Create metrics instance
val metrics = PrometheusConfig.createBatchMetrics("order_processor")

// Use with BatchJobProcessor
val processor = BatchJobProcessor(
    iterator = OrderIterator(),
    submitJob = ::processOrder,
    metrics = metrics,  // Enable metrics!
    jobName = "order_processing"
)
```

### Expose Metrics Endpoint

```kotlin
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Application.configureRouting(registry: PrometheusMeterRegistry) {
    routing {
        get("/metrics") {
            call.respondText(registry.scrape(), ContentType.Text.Plain)
        }
    }
}
```

## 📈 Available Metrics

TaskRunna automatically collects these metrics:

| Metric Name | Type | Description | Tags |
|-------------|------|-------------|------|
| <code>{prefix}_jobs_started_total</code> | Counter | Total batch jobs started | <code>job_name</code> |
| <code>{prefix}_jobs_completed_total</code> | Counter | Total batch jobs completed | <code>job_name</code>, <code>result</code> |
| <code>{prefix}_job_duration_seconds</code> | Timer | Time taken for complete jobs | <code>job_name</code>, <code>result</code> |
| <code>{prefix}_tasks_submitted_total</code> | Counter | Total tasks submitted | <code>job_name</code> |
| <code>{prefix}_tasks_completed_total</code> | Counter | Total tasks completed | <code>job_name</code>, <code>result</code>, <code>error_type</code> |
| <code>{prefix}_task_duration_seconds</code> | Timer | Time taken for individual tasks | <code>job_name</code>, <code>result</code> |
| <code>{prefix}_batches_processed_total</code> | Counter | Total batches processed | <code>job_name</code> |
| <code>{prefix}_items_processed_total</code> | Counter | Total items processed | <code>job_name</code> |

### Metric Tags

- **<code>job_name</code>**: The name you assign to your batch job
- **<code>result</code>**: <code>success</code> or <code>failure</code>
- **<code>error_type</code>**: The exception class name for failed tasks

### Example Metrics Output

```prometheus
# HELP order_retry_jobs_started_total Total batch jobs started
# TYPE order_retry_jobs_started_total counter
order_retry_jobs_started_total{job_name="order_retry_job"} 1.0

# HELP order_retry_tasks_completed_total Total number of tasks completed
# TYPE order_retry_tasks_completed_total counter
order_retry_tasks_completed_total{job_name="order_retry_job",result="success"} 42.0
order_retry_tasks_completed_total{job_name="order_retry_job",result="failure",error_type="PaymentException"} 8.0

# HELP order_retry_task_duration_seconds Time taken to complete individual tasks
# TYPE order_retry_task_duration_seconds summary
order_retry_task_duration_seconds_count{job_name="order_retry_job",result="success"} 42.0
order_retry_task_duration_seconds_sum{job_name="order_retry_job",result="success"} 12.5
```

## 🔧 Configuration

### Custom Metric Prefix

```kotlin
val metrics = MicrometerBatchMetrics(
    meterRegistry = PrometheusMeterRegistry.builder().build(),
    prefix = "my_app_batch"  // Custom prefix
)
```

### Auto-Detection

TaskRunna automatically detects if Prometheus is available:

```kotlin
// Automatically uses PrometheusMeterRegistry if available
val metrics = PrometheusConfig.createBatchMetrics("job_name")

// Falls back to SimpleMeterRegistry if Prometheus not in classpath
// Falls back to NoOpBatchMetrics if Micrometer not available
```

### Disable Metrics

```kotlin
import com.taskrunna.batch.metrics.NoOpBatchMetrics

val processor = BatchJobProcessor(
    iterator = OrderIterator(),
    submitJob = ::processOrder,
    metrics = NoOpBatchMetrics.INSTANCE  // No metrics overhead
)
```

## 📊 Monitoring with PromQL

### Success Rate

```promql
# Overall success rate
rate(order_retry_tasks_completed_total{result="success"}[5m]) / 
rate(order_retry_tasks_completed_total[5m]) * 100
```

### Error Rate by Type

```promql
# Errors by exception type
rate(order_retry_tasks_completed_total{result="failure"}[5m]) by (error_type)
```

### Average Processing Time

```promql
# Average task duration
rate(order_retry_task_duration_seconds_sum[5m]) / 
rate(order_retry_task_duration_seconds_count[5m])
```

### Throughput

```promql
# Tasks processed per second
rate(order_retry_tasks_completed_total[5m])
```

### Job Completion Rate

```promql
# Jobs completed per hour
rate(order_retry_jobs_completed_total[1h]) * 3600
```

## 🚨 Alerting Rules

### High Error Rate

```yaml
groups:
  - name: taskrunna.rules
    rules:
      - alert: TaskRunnaHighErrorRate
        expr: |
          (
            rate(order_retry_tasks_completed_total{result="failure"}[5m]) /
            rate(order_retry_tasks_completed_total[5m])
          ) > 0.1
        for: 2m
        labels:
          severity: warning
        annotations:
          summary: "TaskRunna job {{ $labels.job_name }} has high error rate"
          description: "Error rate is {{ $value | humanizePercentage }} for job {{ $labels.job_name }}"
```

### Job Stalled

```yaml
      - alert: TaskRunnaJobStalled
        expr: |
          increase(order_retry_jobs_started_total[10m]) > 0 and
          increase(order_retry_jobs_completed_total[10m]) == 0
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "TaskRunna job {{ $labels.job_name }} appears stalled"
          description: "Job started but no completion in 10 minutes"
```

### Slow Processing

```yaml
      - alert: TaskRunnaSlowProcessing
        expr: |
          rate(order_retry_task_duration_seconds_sum[5m]) /
          rate(order_retry_task_duration_seconds_count[5m]) > 30
        for: 3m
        labels:
          severity: warning
        annotations:
          summary: "TaskRunna job {{ $labels.job_name }} processing slowly"
          description: "Average task duration is {{ $value }}s"
```

## 📊 Grafana Dashboard

### Key Panels

**1. Job Overview**
```promql
# Jobs started vs completed
increase(order_retry_jobs_started_total[1h])
increase(order_retry_jobs_completed_total[1h])
```

**2. Task Success Rate**
```promql
# Success rate over time
rate(order_retry_tasks_completed_total{result="success"}[5m]) /
rate(order_retry_tasks_completed_total[5m]) * 100
```

**3. Processing Time Distribution**
```promql
# P50, P95, P99 latencies
histogram_quantile(0.50, rate(order_retry_task_duration_seconds_bucket[5m]))
histogram_quantile(0.95, rate(order_retry_task_duration_seconds_bucket[5m]))
histogram_quantile(0.99, rate(order_retry_task_duration_seconds_bucket[5m]))
```

**4. Error Breakdown**
```promql
# Errors by type
rate(order_retry_tasks_completed_total{result="failure"}[5m]) by (error_type)
```

**5. Throughput**
```promql
# Items processed per second
rate(order_retry_items_processed_total[5m])
```

### Dashboard JSON

```json
{
  "dashboard": {
    "title": "TaskRunna Batch Processing",
    "panels": [
      {
        "title": "Success Rate",
        "type": "stat",
        "targets": [
          {
            "expr": "rate(order_retry_tasks_completed_total{result=\"success\"}[5m]) / rate(order_retry_tasks_completed_total[5m]) * 100"
          }
        ]
      }
    ]
  }
}
```

## 🔍 Troubleshooting

### No Metrics Appearing

**Check Dependencies:**
```kotlin
dependencies {
    implementation("io.micrometer:micrometer-registry-prometheus:1.12.0")
}
```

**Verify Metrics are Enabled:**
```kotlin
val metrics = PrometheusConfig.createBatchMetrics("my_job")
// Should not be NoOpBatchMetrics.INSTANCE
```

### Metrics Not Updating

**Ensure Job Name is Set:**
```kotlin
val processor = BatchJobProcessor(
    // ... other parameters
    metrics = metrics,
    jobName = "order_processing"  // Required for proper tagging
)
```

### Memory Usage Concerns

**Use Metric Filters:**
```kotlin
val registry = PrometheusMeterRegistry.builder()
    .meterFilter(MeterFilter.denyNameStartsWith("jvm"))  // Exclude JVM metrics
    .build()

val metrics = MicrometerBatchMetrics(registry, "batch")
```

## 🏭 Production Best Practices

### 1. Metric Retention

Configure appropriate retention in Prometheus:

```yaml
# prometheus.yml
global:
  scrape_interval: 15s
  evaluation_interval: 15s

rule_files:
  - "taskrunna.rules.yml"

scrape_configs:
  - job_name: 'taskrunna'
    static_configs:
      - targets: ['localhost:8080']
    scrape_interval: 30s
    metrics_path: /metrics
```

### 2. Cardinality Management

Be careful with high-cardinality tags:

```kotlin
// Good: Low cardinality
val processor = BatchJobProcessor(
    // ...
    jobName = "order_retry"  // Same for all instances
)

// Bad: High cardinality
val processor = BatchJobProcessor(
    // ...
    jobName = "order_retry_${System.currentTimeMillis()}"  // Unique per run
)
```

### 3. Resource Monitoring

Monitor TaskRunna's resource usage:

```promql
# Memory usage
process_resident_memory_bytes{job="taskrunna"}

# CPU usage
rate(process_cpu_seconds_total{job="taskrunna"}[5m])

# Thread count
process_threads{job="taskrunna"}
```

### 4. SLA Monitoring

Set up SLO/SLI tracking:

```yaml
# SLI: 95% of tasks complete successfully
sli_success_rate: |
  rate(order_retry_tasks_completed_total{result="success"}[5m]) /
  rate(order_retry_tasks_completed_total[5m])

# SLO: Success rate > 95%
slo_target: 0.95
```

## 🔗 Integration Examples

### Spring Boot Actuator

```kotlin
@Component
class TaskRunnaMetricsExporter {
    
    @EventListener
    fun onJobCompleted(event: BatchJobCompletedEvent) {
        meterRegistry.counter("batch.jobs.completed", 
            "job_name", event.jobName,
            "result", if (event.success) "success" else "failure"
        ).increment()
    }
}
```

### Custom Metrics

```kotlin
class CustomMetricsCollector(private val meterRegistry: MeterRegistry) {
    private val businessMetrics = meterRegistry.counter("business.orders.processed")
    
    fun onOrderProcessed(order: Order) {
        businessMetrics.increment(
            Tags.of(
                "customer_tier", order.customerTier,
                "region", order.region
            )
        )
    }
}
```

Want to see metrics in action? [Run the live example](../examples#live-example-order-retry-system)! 