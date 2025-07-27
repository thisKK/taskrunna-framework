package com.taskrunna.batch.metrics

import java.time.Duration

/**
 * No-operation implementation of BatchMetrics that discards all metrics.
 * Used when metrics collection is disabled or not configured.
 */
class NoOpBatchMetrics : BatchMetrics {

    override fun recordJobStart(jobName: String) {
        // No-op
    }

    override fun recordJobCompletion(jobName: String, duration: Duration, success: Boolean) {
        // No-op
    }

    override fun recordBatch(jobName: String, batchNumber: Int, itemCount: Int, duration: Duration) {
        // No-op
    }

    override fun recordTaskSubmitted(jobName: String) {
        // No-op
    }

    override fun recordTaskSuccess(jobName: String, duration: Duration) {
        // No-op
    }

    override fun recordTaskFailure(jobName: String, duration: Duration, errorType: String) {
        // No-op
    }

    override fun recordQueueSize(jobName: String, queueSize: Int) {
        // No-op
    }

    companion object {
        @JvmStatic
        val INSTANCE = NoOpBatchMetrics()
    }
}
