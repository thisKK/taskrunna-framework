# TaskRunna Metrics & Observability 📊

TaskRunna Framework provides comprehensive observability through Prometheus metrics integration, enabling you to monitor batch job performance, identify bottlenecks, and maintain operational excellence.

## 🚀 Quick Start

### Basic Setup

```kotlin
import com.taskrunna.batch.metrics.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

// Create Prometheus registry
val prometheusRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
val metrics = PrometheusConfig.createBatchMetrics(prometheusRegistry, "order_processing")

// Use with BatchJobProcessor
val processor = BatchJobProcessor(
    iterator = orderIterator,
    submitJob = { order -> processOrder(order) },
    onSuccess = { order, result -> markOrderComplete(order.id) },
    onFailure = { order, error -> handleOrderFailure(order.id, error) },
    logger = logger,
    metrics = metrics,
    jobName = "order_retry_job"
)
```

### Metrics Endpoint

Expose metrics for Prometheus scraping:

```kotlin
// Using Ktor
routing {
    get("/metrics") {
        call.respondText(
            prometheusRegistry.scrape(),
            ContentType.Text.Plain
        )
    }
}

// Using Spring Boot
@RestController
class MetricsController(private val registry: PrometheusMeterRegistry) {
    @GetMapping("/metrics", produces = ["text/plain"])
    fun metrics(): String = registry.scrape()
}
```

## 📈 Available Metrics

### Job-Level Metrics

| Metric Name | Type | Description | Tags |
|-------------|------|-------------|------|
| `{prefix}_jobs_started_total` | Counter | Total number of batch jobs started | `job_name` |
| `{prefix}_jobs_completed_total` | Counter | Total number of batch jobs completed | `job_name`, `result` |
| `{prefix}_job_duration_seconds` | Timer | Time taken to complete entire batch jobs | `job_name` |

### Task-Level Metrics

| Metric Name | Type | Description | Tags |
|-------------|------|-------------|------|
| `{prefix}_tasks_submitted_total` | Counter | Total tasks submitted for processing | `job_name` |
| `{prefix}_tasks_completed_total` | Counter | Total tasks completed | `job_name`, `result`, `error_type` |
| `{prefix}_task_duration_seconds` | Timer | Time taken for individual task execution | `job_name` |

### Batch-Level Metrics

| Metric Name | Type | Description | Tags |
|-------------|------|-------------|------|
| `{prefix}_batches_processed_total` | Counter | Total batches processed | `job_name`, `batch_number` |
| `{prefix}_items_processed_total` | Counter | Total items processed across all batches | `job_name` |
| `{prefix}_batch_duration_seconds` | Timer | Time taken to process individual batches | `job_name` |

### Tags Explanation

- **`job_name`**: Identifies the specific batch job (e.g., "order_retry", "data_migration")
- **`result`**: Success or failure status (`success`, `failure`)
- **`error_type`**: Type of error for failed tasks (e.g., "RuntimeException", "TimeoutException")
- **`batch_number`**: Sequential batch number within a job

## 🎯 Monitoring Strategies

### Success Rate Monitoring

```promql
# Overall success rate for all jobs
rate(taskrunna_tasks_completed_total{result="success"}[5m]) / 
rate(taskrunna_tasks_completed_total[5m]) * 100

# Success rate per job
rate(taskrunna_tasks_completed_total{result="success", job_name="order_retry"}[5m]) / 
rate(taskrunna_tasks_completed_total{job_name="order_retry"}[5m]) * 100
```

### Performance Monitoring

```promql
# Average task duration per job
rate(taskrunna_task_duration_seconds_sum{job_name="order_retry"}[5m]) / 
rate(taskrunna_task_duration_seconds_count{job_name="order_retry"}[5m])

# 95th percentile task duration
histogram_quantile(0.95, 
  rate(taskrunna_task_duration_seconds_bucket{job_name="order_retry"}[5m])
)

# Throughput (tasks per second)
rate(taskrunna_tasks_completed_total{job_name="order_retry"}[5m])
```

### Error Rate Monitoring

```promql
# Error rate by error type
rate(taskrunna_tasks_completed_total{result="failure"}[5m]) by (error_type)

# Jobs failing completely
rate(taskrunna_jobs_completed_total{result="failure"}[5m])
```

## 🚨 Alerting Rules

### High Error Rate Alert

```yaml
groups:
- name: taskrunna_alerts
  rules:
  - alert: TaskRunnaHighErrorRate
    expr: |
      (
        rate(taskrunna_tasks_completed_total{result="failure"}[5m]) /
        rate(taskrunna_tasks_completed_total[5m])
      ) > 0.1
    for: 2m
    labels:
      severity: warning
    annotations:
      summary: "TaskRunna job {{ $labels.job_name }} has high error rate"
      description: "Error rate is {{ $value | humanizePercentage }} for job {{ $labels.job_name }}"
```

### Long Running Job Alert

```yaml
- alert: TaskRunnaLongRunningJob
  expr: |
    taskrunna_job_duration_seconds > 3600
  for: 1m
  labels:
    severity: warning
  annotations:
    summary: "TaskRunna job {{ $labels.job_name }} running for over 1 hour"
    description: "Job {{ $labels.job_name }} has been running for {{ $value | humanizeDuration }}"
```

## 📊 Grafana Dashboard

### Key Panels

1. **Job Overview**
   - Total jobs started/completed
   - Overall success rate
   - Active jobs

2. **Performance Metrics**
   - Task duration percentiles
   - Throughput (tasks/sec)
   - Batch processing rates

3. **Error Analysis**
   - Error rate by job
   - Error breakdown by type
   - Failed job trends

### Sample Queries

```json
{
  "title": "Task Success Rate",
  "type": "stat",
  "targets": [
    {
      "expr": "rate(taskrunna_tasks_completed_total{result=\"success\"}[5m]) / rate(taskrunna_tasks_completed_total[5m]) * 100",
      "legendFormat": "Success Rate %"
    }
  ]
}
```

## 🔧 Configuration Options

### Custom Metrics Prefix

```kotlin
val metrics = PrometheusConfig.createBatchMetrics(registry, "my_app")
// Results in metrics like: my_app_jobs_started_total
```

### Disable Metrics

```kotlin
val processor = BatchJobProcessor(
    // ... other params
    metrics = PrometheusConfig.createNoOpBatchMetrics()
)
```

### Custom MeterRegistry

```kotlin
val customRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
// Add custom configuration
val metrics = PrometheusConfig.createBatchMetrics(customRegistry, "custom_prefix")
```

## 🏭 Production Considerations

### Resource Usage

- Metrics collection adds minimal overhead (~1-2% CPU)
- Memory usage scales with number of unique tag combinations
- Consider metric retention policies for high-volume jobs

### Best Practices

1. **Use meaningful job names** that reflect business purpose
2. **Limit tag cardinality** to avoid metrics explosion
3. **Set up proper retention** for time-series data
4. **Monitor metrics endpoints** for availability
5. **Use consistent naming** across your application

### Example Production Setup

```kotlin
@Configuration
class MetricsConfig {
    
    @Bean
    fun prometheusMeterRegistry(): PrometheusMeterRegistry {
        return PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    }
    
    @Bean
    fun batchMetrics(registry: PrometheusMeterRegistry): BatchMetrics {
        return PrometheusConfig.createBatchMetrics(registry, "myapp")
    }
}

@Service
class OrderRetryService(private val metrics: BatchMetrics) {
    
    fun retryFailedOrders() {
        val processor = BatchJobProcessor(
            iterator = FailedOrderIterator(orderRepository),
            submitJob = { order -> orderService.retry(order) },
            onSuccess = { order, _ -> orderRepository.markRetrySuccessful(order.id) },
            onFailure = { order, error -> handleRetryFailure(order, error) },
            logger = logger,
            metrics = metrics,
            jobName = "order_retry"
        )
        processor.run()
    }
}
```

## 🔍 Troubleshooting

### Missing Metrics

1. Verify Prometheus dependency is on classpath
2. Check metrics endpoint is accessible
3. Ensure job names are consistent
4. Validate PrometheusConfig setup

### High Cardinality

1. Review tag usage (especially `batch_number`)
2. Consider aggregating rare error types
3. Implement tag value limits if needed

### Performance Impact

1. Monitor metrics collection overhead
2. Consider sampling for high-volume jobs
3. Use separate thread pools if needed

## 📚 Further Reading

- [Micrometer Documentation](https://micrometer.io/docs)
- [Prometheus Best Practices](https://prometheus.io/docs/practices/)
- [Grafana Dashboard Examples](https://grafana.com/grafana/dashboards/) 