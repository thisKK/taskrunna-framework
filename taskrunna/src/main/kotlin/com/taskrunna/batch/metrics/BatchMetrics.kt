package com.taskrunna.batch.metrics

import java.time.Duration

/**
 * Interface for collecting metrics during batch processing operations.
 * Provides hooks for recording various batch processing events and timings.
 */
interface BatchMetrics {

    /**
     * Record that a batch processing job has started.
     * @param jobName name/type of the batch job
     */
    fun recordJobStart(jobName: String)

    /**
     * Record that a batch processing job has completed.
     * @param jobName name/type of the batch job
     * @param duration total time taken for the job
     * @param success whether the job completed successfully
     */
    fun recordJobCompletion(jobName: String, duration: Duration, success: Boolean)

    /**
     * Record processing of a batch of items.
     * @param jobName name/type of the batch job
     * @param batchNumber the batch sequence number
     * @param itemCount number of items in this batch
     * @param duration time taken to process this batch
     */
    fun recordBatch(jobName: String, batchNumber: Int, itemCount: Int, duration: Duration)

    /**
     * Record submission of a task for processing.
     * @param jobName name/type of the batch job
     */
    fun recordTaskSubmitted(jobName: String)

    /**
     * Record successful completion of a task.
     * @param jobName name/type of the batch job
     * @param duration time taken for task execution
     */
    fun recordTaskSuccess(jobName: String, duration: Duration)

    /**
     * Record failure of a task.
     * @param jobName name/type of the batch job
     * @param duration time taken before failure
     * @param errorType type/class of the error
     */
    fun recordTaskFailure(jobName: String, duration: Duration, errorType: String)

    /**
     * Record current queue size or pending tasks.
     * @param jobName name/type of the batch job
     * @param queueSize current number of pending tasks
     */
    fun recordQueueSize(jobName: String, queueSize: Int)
}
