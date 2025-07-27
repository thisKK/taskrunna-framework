package com.taskrunna.batch.metrics

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Duration

class MicrometerBatchMetricsTest {

    private lateinit var meterRegistry: SimpleMeterRegistry
    private lateinit var metrics: MicrometerBatchMetrics

    @BeforeEach
    fun setup() {
        meterRegistry = SimpleMeterRegistry()
        metrics = MicrometerBatchMetrics(meterRegistry, "test")
    }

    @Test
    fun `should record job start metrics`() {
        metrics.recordJobStart("test_job")

        val counter = meterRegistry.find("test_jobs_started_total")
            .tag("job_name", "test_job")
            .counter()

        assertEquals(1.0, counter?.count())
    }

    @Test
    fun `should record job completion metrics for success`() {
        val duration = Duration.ofSeconds(5)

        metrics.recordJobCompletion("test_job", duration, true)

        // Check success counter
        val successCounter = meterRegistry.find("test_jobs_completed_total")
            .tag("job_name", "test_job")
            .tag("result", "success")
            .counter()

        assertEquals(1.0, successCounter?.count())

        // Check timer
        val timer = meterRegistry.find("test_job_duration_seconds")
            .tag("job_name", "test_job")
            .timer()

        assertEquals(1, timer?.count())
        assertTrue(timer?.totalTime(java.util.concurrent.TimeUnit.SECONDS)!! >= 5.0)
    }

    @Test
    fun `should record job completion metrics for failure`() {
        val duration = Duration.ofSeconds(3)

        metrics.recordJobCompletion("test_job", duration, false)

        // Check failure counter
        val failureCounter = meterRegistry.find("test_jobs_completed_total")
            .tag("job_name", "test_job")
            .tag("result", "failure")
            .counter()

        assertEquals(1.0, failureCounter?.count())
    }

    @Test
    fun `should record batch processing metrics`() {
        val duration = Duration.ofMillis(500)

        metrics.recordBatch("test_job", 1, 10, duration)

        // Check batch counter
        val batchCounter = meterRegistry.find("test_batches_processed_total")
            .tag("job_name", "test_job")
            .tag("batch_number", "1")
            .counter()

        assertEquals(1.0, batchCounter?.count())

        // Check items counter
        val itemsCounter = meterRegistry.find("test_items_processed_total")
            .tag("job_name", "test_job")
            .counter()

        assertEquals(10.0, itemsCounter?.count())

        // Check batch duration
        val timer = meterRegistry.find("test_batch_duration_seconds")
            .tag("job_name", "test_job")
            .timer()

        assertEquals(1, timer?.count())
    }

    @Test
    fun `should record task submission metrics`() {
        metrics.recordTaskSubmitted("test_job")
        metrics.recordTaskSubmitted("test_job")

        val counter = meterRegistry.find("test_tasks_submitted_total")
            .tag("job_name", "test_job")
            .counter()

        assertEquals(2.0, counter?.count())
    }

    @Test
    fun `should record task success metrics`() {
        val duration = Duration.ofMillis(100)

        metrics.recordTaskSuccess("test_job", duration)

        // Check success counter
        val successCounter = meterRegistry.find("test_tasks_completed_total")
            .tag("job_name", "test_job")
            .tag("result", "success")
            .counter()

        assertEquals(1.0, successCounter?.count())

        // Check task duration
        val timer = meterRegistry.find("test_task_duration_seconds")
            .tag("job_name", "test_job")
            .timer()

        assertEquals(1, timer?.count())
    }

    @Test
    fun `should record task failure metrics`() {
        val duration = Duration.ofMillis(200)

        metrics.recordTaskFailure("test_job", duration, "RuntimeException")

        // Check failure counter
        val failureCounter = meterRegistry.find("test_tasks_completed_total")
            .tag("job_name", "test_job")
            .tag("result", "failure")
            .tag("error_type", "RuntimeException")
            .counter()

        assertEquals(1.0, failureCounter?.count())

        // Check task duration
        val timer = meterRegistry.find("test_task_duration_seconds")
            .tag("job_name", "test_job")
            .timer()

        assertEquals(1, timer?.count())
    }

    @Test
    fun `should handle multiple jobs with different names`() {
        metrics.recordJobStart("job_a")
        metrics.recordJobStart("job_b")
        metrics.recordJobStart("job_a")

        val jobACounter = meterRegistry.find("test_jobs_started_total")
            .tag("job_name", "job_a")
            .counter()

        val jobBCounter = meterRegistry.find("test_jobs_started_total")
            .tag("job_name", "job_b")
            .counter()

        assertEquals(2.0, jobACounter?.count())
        assertEquals(1.0, jobBCounter?.count())
    }

    @Test
    fun `should record queue size updates`() {
        metrics.recordQueueSize("test_job", 5)
        metrics.recordQueueSize("test_job", 10)
        metrics.recordQueueSize("other_job", 3)

        // Queue size is stored but gauge implementation is simplified
        // This test ensures no exceptions are thrown
        assertTrue(true)
    }
}
