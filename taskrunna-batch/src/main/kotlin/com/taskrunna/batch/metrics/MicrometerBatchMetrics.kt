package com.taskrunna.batch.metrics

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Micrometer-based implementation of BatchMetrics.
 * Collects comprehensive metrics using Micrometer that can be exported to Prometheus and other systems.
 */
class MicrometerBatchMetrics(
    private val meterRegistry: MeterRegistry,
    private val metricsPrefix: String = "taskrunna",
) : BatchMetrics {

    // Gauges for current state
    private val queueSizes = ConcurrentHashMap<String, AtomicInteger>()

    override fun recordJobStart(jobName: String) {
        Counter.builder("${metricsPrefix}_jobs_started_total")
            .description("Total number of batch jobs started")
            .tag("job_name", jobName)
            .register(meterRegistry)
            .increment()
    }

    override fun recordJobCompletion(jobName: String, duration: Duration, success: Boolean) {
        val result = if (success) "success" else "failure"

        Counter.builder("${metricsPrefix}_jobs_completed_total")
            .description("Total number of batch jobs completed")
            .tag("job_name", jobName)
            .tag("result", result)
            .register(meterRegistry)
            .increment()

        Timer.builder("${metricsPrefix}_job_duration_seconds")
            .description("Time taken to complete batch jobs")
            .tag("job_name", jobName)
            .register(meterRegistry)
            .record(duration)
    }

    override fun recordBatch(jobName: String, batchNumber: Int, itemCount: Int, duration: Duration) {
        Counter.builder("${metricsPrefix}_batches_processed_total")
            .description("Total number of batches processed")
            .tag("job_name", jobName)
            .tag("batch_number", batchNumber.toString())
            .register(meterRegistry)
            .increment()

        Counter.builder("${metricsPrefix}_items_processed_total")
            .description("Total number of items processed across all batches")
            .tag("job_name", jobName)
            .register(meterRegistry)
            .increment(itemCount.toDouble())

        Timer.builder("${metricsPrefix}_batch_duration_seconds")
            .description("Time taken to process individual batches")
            .tag("job_name", jobName)
            .register(meterRegistry)
            .record(duration)
    }

    override fun recordTaskSubmitted(jobName: String) {
        Counter.builder("${metricsPrefix}_tasks_submitted_total")
            .description("Total number of tasks submitted for processing")
            .tag("job_name", jobName)
            .register(meterRegistry)
            .increment()
    }

    override fun recordTaskSuccess(jobName: String, duration: Duration) {
        Counter.builder("${metricsPrefix}_tasks_completed_total")
            .description("Total number of tasks completed")
            .tag("job_name", jobName)
            .tag("result", "success")
            .register(meterRegistry)
            .increment()

        Timer.builder("${metricsPrefix}_task_duration_seconds")
            .description("Time taken to complete individual tasks")
            .tag("job_name", jobName)
            .register(meterRegistry)
            .record(duration)
    }

    override fun recordTaskFailure(jobName: String, duration: Duration, errorType: String) {
        Counter.builder("${metricsPrefix}_tasks_completed_total")
            .description("Total number of tasks completed")
            .tag("job_name", jobName)
            .tag("result", "failure")
            .tag("error_type", errorType)
            .register(meterRegistry)
            .increment()

        Timer.builder("${metricsPrefix}_task_duration_seconds")
            .description("Time taken to complete individual tasks")
            .tag("job_name", jobName)
            .register(meterRegistry)
            .record(duration)
    }

    override fun recordQueueSize(jobName: String, queueSize: Int) {
        // TODO: Implement queue size gauge when needed
        // For now, just store the value for potential future use
        queueSizes.computeIfAbsent(jobName) { AtomicInteger(0) }.set(queueSize)
    }
}
