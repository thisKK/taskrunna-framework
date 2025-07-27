package com.taskrunna.batch

import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.taskrunna.batch.metrics.BatchMetrics
import com.taskrunna.batch.metrics.NoOpBatchMetrics
import com.taskrunna.core.BaseBatchIterator
import io.github.oshai.kotlinlogging.KLogger
import java.util.concurrent.Executors

/**
 * BatchJobProcessor is a generic utility to process data in batches with asynchronous job submission.
 *
 * It is especially useful in microservices for processing jobs such as:
 * - DLQ (Dead Letter Queue) retries
 * - Recalculation tasks
 * - Bulk updates or notification sends
 *
 * @param T the type of item to process
 * @param R the result type returned by submitJob
 *
 * Example Usage:
 *
 * ```kotlin
 * val processor = BatchJobProcessor(
 *     iterator = MyBatchIterator(myRepository),
 *     submitJob = { item ->
 *         // Submit async task (e.g., send to Kafka or call HTTP)
 *         JdkFutureAdapters.listenInPoolThread(myAsyncService.send(item))
 *     },
 *     onSuccess = { item, result ->
 *         // Handle success (e.g., mark DB record as sent)
 *         myRepository.markAsSent(item.id)
 *     },
 *     onFailure = { item, error ->
 *         // Handle failure (e.g., log, increment retry count)
 *         logger.error(error) { "Failed to process ${item.id}" }
 *     },
 * )
 *
 * processor.run()
 * ```
 *
 * Notes:
 * - The iterator must extend BaseBatchIterator<T>, which paginates your data source.
 * - submitJob returns a ListenableFuture<R>, allowing non-blocking job execution.
 * - onSuccess/onFailure are called when the async job completes.
 */
class BatchJobProcessor<T, R>(
    private val iterator: BaseBatchIterator<T>,
    private val submitJob: (T) -> ListenableFuture<R>,
    private val onSuccess: (T, R?) -> Unit = { _, _ -> },
    private val onFailure: (T, Throwable) -> Unit = { _, _ -> },
    private val logger: KLogger,
    private val metrics: BatchMetrics = NoOpBatchMetrics.INSTANCE,
    private val jobName: String = "batch_job",
) {

    private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1)
    private val batchJobStats = BatchJobStats()

    fun run() {
        val jobStartTime = System.currentTimeMillis()
        metrics.recordJobStart(jobName)
        logger.info { "[BatchJob] Starting..." }

        var batchNumber = 0
        var totalSuccess = true

        try {
            while (iterator.hasNext()) {
                batchNumber++
                val batchStartTime = System.currentTimeMillis()
                val batch = iterator.next()

                if (batch.isEmpty()) {
                    logger.info { "[BatchJob] No records to process. Job finished." }
                    return
                }

                logger.info { "[BatchJob] Retrieved ${batch.size} record(s)." }

                batch.forEach { item ->
                    val taskStartTime = System.currentTimeMillis()
                    val future = submitJob(item).also {
                        batchJobStats.submit()
                        metrics.recordTaskSubmitted(jobName)
                    }

                    batchJobStats.listenToStatsOnly(future)

                    Futures.addCallback(
                        future,
                        object : FutureCallback<R?> {
                            override fun onSuccess(result: R?) {
                                val duration = java.time.Duration.ofMillis(System.currentTimeMillis() - taskStartTime)
                                metrics.recordTaskSuccess(jobName, duration)
                                onSuccess(item, result)
                            }

                            override fun onFailure(t: Throwable) {
                                val duration = java.time.Duration.ofMillis(System.currentTimeMillis() - taskStartTime)
                                val errorType = t.javaClass.simpleName
                                metrics.recordTaskFailure(jobName, duration, errorType)
                                onFailure(item, t)
                                totalSuccess = false
                            }
                        },
                        executor,
                    )
                }

                val batchDuration = java.time.Duration.ofMillis(System.currentTimeMillis() - batchStartTime)
                metrics.recordBatch(jobName, batchNumber, batch.size, batchDuration)

                iterator.reportBatch()
            }

            batchJobStats.awaitProcessingAndTerminate()
            logger.info { "[BatchJob] All jobs submitted. Job completed." }
        } finally {
            val jobDuration = java.time.Duration.ofMillis(System.currentTimeMillis() - jobStartTime)
            metrics.recordJobCompletion(jobName, jobDuration, totalSuccess)
        }
    }
}
