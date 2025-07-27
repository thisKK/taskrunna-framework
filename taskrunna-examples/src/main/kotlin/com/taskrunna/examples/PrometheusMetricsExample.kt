package com.taskrunna.examples

import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.taskrunna.batch.BaseBatchIterator
import com.taskrunna.batch.BatchJobProcessor
import com.taskrunna.batch.metrics.MicrometerBatchMetrics
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import kotlinx.coroutines.runBlocking
import java.util.concurrent.CompletableFuture
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

// Example data classes
data class OrderRetry(val id: String, val orderId: String, val attempt: Int)
data class ProcessingResult(val success: Boolean, val message: String)

// Example iterator that simulates failed orders needing retry
class FailedOrderIterator(
    private val totalOrders: Int = 100,
    batchSize: Int = 10,
) : BaseBatchIterator<OrderRetry>(batchSize) {

    private var processedCount = 0

    override fun loadNextBatch(afterCursor: String, batchSize: Int): Collection<OrderRetry> {
        if (processedCount >= totalOrders) return emptyList()

        val batchStart = processedCount + 1
        val batchEnd = minOf(processedCount + batchSize, totalOrders)

        val batch = (batchStart..batchEnd).map { i ->
            OrderRetry(
                id = "retry-$i",
                orderId = "order-${1000 + i}",
                attempt = Random.nextInt(1, 4),
            )
        }

        processedCount = batchEnd
        return batch
    }

    override fun extractCursorFrom(item: OrderRetry): String = item.id
}

// Simulates an async service that processes order retries
class OrderRetryService {

    suspend fun retryOrder(orderRetry: OrderRetry): ProcessingResult {
        // Simulate processing time
        kotlinx.coroutines.delay(Random.nextLong(50, 200))

        // Simulate some failures (20% failure rate)
        val success = Random.nextDouble() > 0.2

        return if (success) {
            ProcessingResult(true, "Order ${orderRetry.orderId} processed successfully")
        } else {
            ProcessingResult(false, "Failed to process order ${orderRetry.orderId}")
        }
    }

    fun retryOrderAsync(orderRetry: OrderRetry): ListenableFuture<ProcessingResult> {
        val future = CompletableFuture<ProcessingResult>()

        // Simulate async processing
        Thread {
            try {
                Thread.sleep(Random.nextLong(50, 200))
                val success = Random.nextDouble() > 0.2

                val result = if (success) {
                    ProcessingResult(true, "Order ${orderRetry.orderId} processed successfully")
                } else {
                    ProcessingResult(false, "Failed to process order ${orderRetry.orderId}")
                }

                future.complete(result)
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }.start()

        return Futures.immediateFuture(future.get())
    }
}

/**
 * Example demonstrating TaskRunna Framework with Prometheus metrics integration.
 * This example shows:
 * 1. Setting up Prometheus metrics collection
 * 2. Processing batch jobs with full observability
 * 3. Exposing metrics via HTTP endpoint for Prometheus scraping
 * 4. Real-world scenario: retrying failed orders
 */
fun main() = runBlocking {
    // Setup Prometheus registry
    val prometheusRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    val metrics = MicrometerBatchMetrics(prometheusRegistry, "order_retry")

    // Setup services
    val orderRetryService = OrderRetryService()

    // Create batch processor with metrics
    val processor = BatchJobProcessor(
        iterator = FailedOrderIterator(totalOrders = 50, batchSize = 5),
        submitJob = { orderRetry ->
            orderRetryService.retryOrderAsync(orderRetry)
        },
        onSuccess = { orderRetry, result ->
            if (result?.success == true) {
                logger.info { "✅ Successfully retried order ${orderRetry.orderId}" }
            } else {
                logger.warn { "⚠️ Retry completed but failed: ${orderRetry.orderId}" }
            }
        },
        onFailure = { orderRetry, error ->
            logger.error(error) { "❌ Failed to retry order ${orderRetry.orderId}" }
        },
        logger = logger,
        metrics = metrics,
        jobName = "order_retry_job",
    )

    // Start HTTP server to expose metrics
    val server = embeddedServer(Netty, port = 8080) {
        routing {
            get("/") {
                call.respondText(
                    """
                    TaskRunna Framework - Prometheus Metrics Example
                    
                    Available endpoints:
                    - GET /metrics - Prometheus metrics
                    - GET /health - Health check
                    
                    The batch job will process 50 failed orders in batches of 5.
                    Monitor the metrics at /metrics to see real-time processing stats.
                    """.trimIndent(),
                )
            }

            get("/metrics") {
                call.respondText(
                    prometheusRegistry.scrape(),
                    io.ktor.http.ContentType.Text.Plain,
                )
            }

            get("/health") {
                call.respondText("OK")
            }
        }
    }

    server.start(wait = false)
    logger.info { "🚀 Started metrics server on http://localhost:8080" }
    logger.info { "📊 Prometheus metrics available at http://localhost:8080/metrics" }

    // Run the batch job
    logger.info { "🔄 Starting order retry batch job..." }
    processor.run()
    logger.info { "✅ Batch job completed!" }

    // Keep server running to allow metrics scraping
    logger.info { "🔍 Server still running for metrics inspection. Press Ctrl+C to stop." }
    logger.info { "🔍 Check metrics at: http://localhost:8080/metrics" }

    // Add shutdown hook
    Runtime.getRuntime().addShutdownHook(
        Thread {
            server.stop(1000, 5000)
            logger.info { "Server stopped" }
        },
    )

    // Keep the main thread alive
    try {
        kotlinx.coroutines.delay(Long.MAX_VALUE)
    } catch (e: Exception) {
        logger.info { "Shutting down..." }
    }
}
