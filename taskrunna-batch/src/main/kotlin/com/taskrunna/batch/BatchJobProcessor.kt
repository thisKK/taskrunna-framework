package com.taskrunna.batch

import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
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
) {

    private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1)
    private val batchJobStats = BatchJobStats()
    
    fun run() {
        logger.info { "[BatchJob] Starting..." }

        while (iterator.hasNext()) {
            val batch = iterator.next()
            if (batch.isEmpty()) {
                logger.info { "[BatchJob] No records to process. Job finished." }
                return
            }

            logger.info { "[BatchJob] Retrieved ${batch.size} record(s)." }

            batch.forEach { item ->
                val future = submitJob(item).also { batchJobStats.submit() }

                batchJobStats.listenToStatsOnly(future)

                Futures.addCallback(
                    future,
                    object : FutureCallback<R?> {
                        override fun onSuccess(result: R?) {
                            onSuccess(item, result)
                        }

                        override fun onFailure(t: Throwable) {
                            onFailure(item, t)
                        }
                    },
                    executor,
                )
            }

            iterator.reportBatch()
        }

        batchJobStats.awaitProcessingAndTerminate()
        logger.info { "[BatchJob] All jobs submitted. Job completed." }
    }
} 