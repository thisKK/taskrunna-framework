package com.taskrunna.batch

import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import io.github.oshai.kotlinlogging.KotlinLogging
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

private val logger = KotlinLogging.logger {}

class BatchJobStats {
    val taskCount = AtomicInteger(0)
    val successCount = AtomicInteger(0)
    val failCount = AtomicInteger(0)

    private val processStartTimeMillis = System.currentTimeMillis()

    private val executor = Executors.newSingleThreadExecutor()

    fun submit() {
        taskCount.incrementAndGet()
    }

    fun awaitProcessingAndTerminate() {
        waitGracePeriodForTasksSubmission()
        shutDownExecutor()
        reportStats()
    }

    fun <R> listenToStatsOnly(future: ListenableFuture<R>) {
        Futures.addCallback(
            future,
            object : FutureCallback<R> {
                override fun onSuccess(result: R) {
                    success()
                }

                override fun onFailure(t: Throwable) {
                    failure()
                }
            },
            executor,
        )
    }

    private fun waitGracePeriodForTasksSubmission() {
        val startGracefulShutdown = System.currentTimeMillis()
        while (notAllTasksProcessedYet() &&
            notExceedTimeLimitSince(startGracefulShutdown)
        ) {
            Thread.sleep(100)
        }
    }

    private fun notExceedTimeLimitSince(startGracefulShutdown: Long): Boolean {
        return System.currentTimeMillis() - startGracefulShutdown < TimeUnit.MINUTES.toMillis(30)
    }

    private fun notAllTasksProcessedYet() = successCount.get() + failCount.get() < taskCount.get()

    private fun shutDownExecutor() {
        executor.shutdown()
        if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
            executor.shutdownNow()
        }
    }

    fun success() {
        successCount.incrementAndGet()
    }

    fun failure() {
        failCount.incrementAndGet()
    }

    private fun reportStats() {
        val totalTasks = taskCount.get()
        val totalSucceeded = successCount.get()
        val totalFailed = failCount.get()
        logger.info {
            "Completed processing $totalTasks records — $totalSucceeded succeeded (${
                getSuccessRate(
                    totalSucceeded,
                    totalTasks,
                )
            }%) in ${
                System.currentTimeMillis().minus(processStartTimeMillis).toSeconds()
            } seconds. $totalFailed records failed"
        }
    }

    private fun getSuccessRate(totalSuccess: Int, totalProcessed: Int): BigDecimal {
        if (totalProcessed == 0) return BigDecimal.ZERO
        return (totalSuccess * 100.0 / totalProcessed).toBigDecimal().setScale(2, RoundingMode.HALF_UP)
    }
}

fun Long.toSeconds(): String =
    ((this) / 1000.0)
        .toBigDecimal()
        .setScale(2, RoundingMode.HALF_UP)
        .toPlainString() 