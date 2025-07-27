package com.taskrunna.batch

import com.google.common.util.concurrent.JdkFutureAdapters
import com.google.common.util.concurrent.ListenableFuture
import io.mockk.mockk
import org.apache.kafka.clients.producer.RecordMetadata
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

class BatchJobStatsTest {
    @Test
    fun `should track submitted task count`() {
        val stats = BatchJobStats()

        stats.submit()
        stats.submit()

        assertEquals(2, stats.taskCount.get())
    }

    @Test
    fun `stats processing should wait task for complete gracefully before executor was terminated - success task`() {
        val stats = BatchJobStats()
        stats.submit()

        val work: CompletableFuture<RecordMetadata> = CompletableFuture()
        val listenableFuture: ListenableFuture<RecordMetadata> =
            JdkFutureAdapters.listenInPoolThread(work)

        stats.listenToStatsOnly(listenableFuture)

        val separateThreadExecutor = Executors.newFixedThreadPool(1)
        val testResult =
            separateThreadExecutor.submit {
                stats.awaitProcessingAndTerminate()

                val all = stats.taskCount.get()
                require(all > 0)

                val success = stats.successCount.get()
                val failed = stats.failCount.get()
                assertThat("Not all tasks are reported to stats", all, `is`(success + failed))
            }
        work.complete(mockk())

        testResult.get()
    }

    @Test
    fun `stats processing should wait task for complete gracefully before executor was terminated - fail task`() {
        val stats = BatchJobStats()
        stats.submit()

        val work: CompletableFuture<RecordMetadata> = CompletableFuture()
        val listenableFuture: ListenableFuture<RecordMetadata> =
            JdkFutureAdapters.listenInPoolThread(work)
        stats.listenToStatsOnly(listenableFuture)

        val separateThreadExecutor = Executors.newFixedThreadPool(1)
        val testResult =
            separateThreadExecutor.submit {
                stats.awaitProcessingAndTerminate()

                val all = stats.taskCount.get()
                require(all > 0)

                val success = stats.successCount.get()
                val failed = stats.failCount.get()
                assertThat("Not all tasks are reported to stats", all, `is`(success + failed))
            }
        work.completeExceptionally(RuntimeException("OOOPPS!"))

        testResult.get()
    }
} 