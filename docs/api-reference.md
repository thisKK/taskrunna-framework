---
layout: default
title: API Reference
permalink: /api-reference/
---

# 📖 API Reference

<div class="wrapper">

Complete reference for all TaskRunna classes and interfaces.

## Core Classes

### BatchJobProcessor

The main entry point for batch processing jobs.

```kotlin
class BatchJobProcessor<T>(
    iterator: BaseBatchIterator<T>,
    submitJob: (T) -> ListenableFuture<*>,
    onSuccess: (T, Any) -> Unit = { _, _ -> },
    onFailure: (T, Throwable) -> Unit = { _, _ -> },
    logger: KLogger,
    metrics: BatchMetrics = NoOpBatchMetrics.INSTANCE,
    jobName: String = "batch_job"
)
```

#### Parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| <code>iterator</code> | <code>BaseBatchIterator&lt;T&gt;</code> | Iterator that loads data in batches |
| <code>submitJob</code> | <code>(T) -&gt; ListenableFuture&lt;*&gt;</code> | Function that processes each item asynchronously |
| <code>onSuccess</code> | <code>(T, Any) -&gt; Unit</code> | Callback invoked when an item is processed successfully |
| <code>onFailure</code> | <code>(T, Throwable) -&gt; Unit</code> | Callback invoked when an item fails to process |
| <code>logger</code> | <code>KLogger</code> | Logger instance for job execution logging |
| <code>metrics</code> | <code>BatchMetrics</code> | Metrics collector (optional, defaults to no-op) |
| <code>jobName</code> | <code>String</code> | Name for the job (used in logging and metrics) |

#### Methods

##### <code>run(): Unit</code>

Executes the batch job, processing all available items.

```kotlin
processor.run()
```

**Behavior:**
- Loads data in batches using the iterator
- Submits each item for async processing
- Waits for all items in a batch to complete before loading the next batch
- Calls success/failure callbacks appropriately
- Logs progress and statistics
- Records metrics if enabled

**Example:**
```kotlin
val processor = BatchJobProcessor(
    iterator = OrderIterator(),
    submitJob = ::processOrder,
    onSuccess = { order, result -> logger.info { "Processed $order" } },
    onFailure = { order, error -> logger.error(error) { "Failed $order" } },
    logger = logger
)

processor.run() // Processes all orders
```

---

### BaseBatchIterator

Abstract base class for implementing batch data iteration with cursor-based pagination.

```kotlin
abstract class BaseBatchIterator<T> {
    abstract fun loadNextBatch(afterCursor: String, batchSize: Int): List<T>
    abstract fun extractCursorFrom(item: T): String
    
    open val defaultBatchSize: Int = 20
}
```

#### Abstract Methods

##### <code>loadNextBatch(afterCursor: String, batchSize: Int): List&lt;T&gt;</code>

Loads the next batch of items for processing.

**Parameters:**
- <code>afterCursor</code>: Cursor indicating where to start loading (empty string for first batch)
- <code>batchSize</code>: Maximum number of items to load

**Returns:** List of items to process (empty list indicates no more data)

**Example:**
```kotlin
override fun loadNextBatch(afterCursor: String, batchSize: Int): List<Order> {
    return orderRepository.findFailedOrders(
        afterId = afterCursor.takeIf { it.isNotEmpty() },
        limit = batchSize
    )
}
```

##### <code>extractCursorFrom(item: T): String</code>

Extracts a cursor value from an item for pagination.

**Parameters:**
- <code>item</code>: The item to extract cursor from

**Returns:** String cursor value (typically an ID or timestamp)

**Example:**
```kotlin
override fun extractCursorFrom(item: Order): String = item.id
```

#### Properties

##### <code>defaultBatchSize: Int</code>

Default batch size when not specified. Override to customize.

```kotlin
override val defaultBatchSize: Int = 50
```

---

### BatchJobStats

Tracks statistics during batch job execution.

```kotlin
class BatchJobStats(
    private val logger: KLogger,
    private val onComplete: (BatchJobStats) -> Unit = {}
)
```

#### Properties

| Property | Type | Description |
|----------|------|-------------|
| <code>tasksSubmitted</code> | <code>AtomicInteger</code> | Number of tasks submitted for processing |
| <code>tasksCompleted</code> | <code>AtomicInteger</code> | Number of tasks completed (success + failure) |
| <code>tasksSucceeded</code> | <code>AtomicInteger</code> | Number of tasks that completed successfully |
| <code>tasksFailed</code> | <code>AtomicInteger</code> | Number of tasks that failed |
| <code>startTime</code> | <code>Instant</code> | When the job started |
| <code>endTime</code> | <code>Instant?</code> | When the job completed (null if still running) |

#### Methods

##### <code>reportStats(): String</code>

Returns a formatted string with current statistics.

```kotlin
val stats = processor.stats.reportStats()
// Returns: "Completed processing 100 records — 95 succeeded (95.00%) in 2.34 seconds. 5 records failed"
```

---

## Metrics

### BatchMetrics

Interface for collecting batch processing metrics.

```kotlin
interface BatchMetrics {
    fun recordJobStarted(jobName: String)
    fun recordJobCompleted(jobName: String, success: Boolean, duration: Duration)
    fun recordTaskSubmitted(jobName: String)
    fun recordTaskCompleted(jobName: String, success: Boolean, duration: Duration, errorType: String?)
    fun recordBatchProcessed(jobName: String, batchSize: Int)
    fun recordItemsProcessed(jobName: String, count: Int)
    fun recordQueueSize(jobName: String, size: Int)
}
```

### MicrometerBatchMetrics

Prometheus metrics implementation using Micrometer.

```kotlin
class MicrometerBatchMetrics(
    private val meterRegistry: MeterRegistry,
    private val prefix: String = "batch"
) : BatchMetrics
```

#### Usage

```kotlin
val registry = PrometheusMeterRegistry.builder().build()
val metrics = MicrometerBatchMetrics(registry, "order_processing")

val processor = BatchJobProcessor(
    iterator = OrderIterator(),
    submitJob = ::processOrder,
    metrics = metrics,
    jobName = "order_retry"
)
```

### NoOpBatchMetrics

No-operation metrics implementation (zero overhead).

```kotlin
object NoOpBatchMetrics : BatchMetrics {
    // All methods are empty implementations
}
```

### PrometheusConfig

Utility for creating and configuring Prometheus metrics.

```kotlin
object PrometheusConfig {
    fun createBatchMetrics(prefix: String): BatchMetrics
    fun createBatchMetrics(registry: PrometheusMeterRegistry, prefix: String): BatchMetrics
}
```

#### Methods

##### <code>createBatchMetrics(prefix: String): BatchMetrics</code>

Auto-detects available dependencies and creates appropriate metrics implementation.

```kotlin
val metrics = PrometheusConfig.createBatchMetrics("order_processor")
// Returns MicrometerBatchMetrics if Prometheus available
// Falls back to NoOpBatchMetrics if not available
```

---

## Configuration

### Thread Pool Configuration

BatchJobProcessor uses an internal thread pool for async execution. The pool is automatically sized based on available processors.

**Default Configuration:**
- Core pool size: <code>Runtime.getRuntime().availableProcessors()</code>
- Maximum pool size: <code>Runtime.getRuntime().availableProcessors() * 2</code>
- Keep alive time: 60 seconds
- Queue: Unbounded LinkedBlockingQueue

### Batch Size Configuration

Control batch size through your iterator implementation:

```kotlin
class OrderIterator : BaseBatchIterator<Order>() {
    override val defaultBatchSize = 100  // Process 100 orders per batch
    
    override fun loadNextBatch(afterCursor: String, batchSize: Int): List<Order> {
        // Use the provided batchSize parameter
        return repository.findOrders(afterCursor, batchSize)
    }
}
```

### Error Handling Configuration

Configure error handling through callbacks:

```kotlin
val processor = BatchJobProcessor(
    iterator = OrderIterator(),
    submitJob = ::processOrder,
    onFailure = { order, error ->
        when (error) {
            is TimeoutException -> {
                logger.warn { "Order ${order.id} timed out, will retry later" }
                retryQueue.add(order)
            }
            is ValidationException -> {
                logger.error { "Order ${order.id} has invalid data: ${error.message}" }
                invalidOrderQueue.add(order)
            }
            else -> {
                logger.error(error) { "Unexpected error processing order ${order.id}" }
                deadLetterQueue.add(order)
            }
        }
    }
)
```

## Production Configuration

### Logging Configuration

TaskRunna uses KotlinLogging. Configure your logger appropriately:

```xml
<!-- logback.xml -->
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>
    
    <logger name="com.taskrunna" level="INFO"/>
    
    <root level="INFO">
        <appender-ref ref="STDOUT"/>
    </root>
</configuration>
```

### Metrics Configuration

For production, configure appropriate metric collection:

```kotlin
val registry = PrometheusMeterRegistry.builder()
    .commonTags("application", "order-service", "environment", "production")
    .meterFilter(MeterFilter.deny(id -> id.getName().startsWith("jvm")))  // Exclude JVM metrics
    .build()

val metrics = MicrometerBatchMetrics(registry, "order_batch")
```

### Resource Monitoring

Monitor TaskRunna's resource usage:

```kotlin
// Add JVM metrics if needed
new JvmMemoryMetrics().bindTo(registry)
new JvmGcMetrics().bindTo(registry)
new ProcessorMetrics().bindTo(registry)
new JvmThreadMetrics().bindTo(registry)
```

## Best Practices

### Iterator Implementation

1. **Efficient Pagination**: Use indexed columns for cursor-based pagination
2. **Consistent Ordering**: Ensure consistent ordering to avoid skipping/duplicating items
3. **Empty List Termination**: Return empty list when no more data available
4. **Exception Handling**: Handle database exceptions gracefully

```kotlin
class EfficientOrderIterator : BaseBatchIterator<Order>() {
    override fun loadNextBatch(afterCursor: String, batchSize: Int): List<Order> {
        return try {
            orderRepository.findFailedOrdersAfter(
                afterId = afterCursor.takeIf { it.isNotEmpty() }?.toLong(),
                limit = batchSize
            ).also { orders ->
                logger.debug { "Loaded ${orders.size} orders after cursor '$afterCursor'" }
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to load orders after cursor '$afterCursor'" }
            emptyList() // Fail gracefully
        }
    }
}
```

### Job Processing

1. **Idempotent Operations**: Ensure job operations are idempotent
2. **Timeout Handling**: Set appropriate timeouts for async operations
3. **Resource Cleanup**: Clean up resources in failure scenarios
4. **Graceful Degradation**: Handle partial failures appropriately

```kotlin
fun processOrderWithTimeout(order: Order): ListenableFuture<String> {
    val future = SettableFuture.create<String>()
    
    val timeoutFuture = Executors.newSingleThreadScheduledExecutor().schedule({
        future.setException(TimeoutException("Order processing timed out after 30s"))
    }, 30, TimeUnit.SECONDS)
    
    paymentService.processAsync(order).addListener({
        timeoutFuture.cancel(false)
        try {
            val result = paymentService.processAsync(order).get()
            future.set("Successfully processed order ${order.id}: $result")
        } catch (e: Exception) {
            future.setException(e)
        }
    }, MoreExecutors.directExecutor())
    
    return future
}
```

### Error Handling

1. **Categorize Errors**: Handle different error types appropriately
2. **Retry Strategies**: Implement exponential backoff for transient failures
3. **Dead Letter Queues**: Use DLQs for permanently failed items
4. **Alerting**: Set up appropriate alerts for error rates

### Monitoring

1. **Set Up Dashboards**: Create Grafana dashboards for key metrics
2. **Configure Alerts**: Set up alerts for high error rates, slow processing, etc.
3. **SLA Monitoring**: Track SLAs and SLOs
4. **Capacity Planning**: Monitor resource usage for capacity planning

## Migration Guide

### From v1.0.0 to v1.1.0

The main change is package structure:

```kotlin
// Before (v1.0.0)
import com.taskrunna.core.BaseBatchIterator
import com.taskrunna.batch.BatchJobProcessor

// After (v1.1.0)
import com.taskrunna.batch.BaseBatchIterator  // Moved to batch package
import com.taskrunna.batch.BatchJobProcessor
```

Dependencies also simplified:

```kotlin
// Before (v1.0.0)
dependencies {
    implementation("com.taskrunna:taskrunna-core:1.0.0")
    implementation("com.taskrunna:taskrunna-batch:1.0.0")
}

// After (v1.1.0)
dependencies {
    implementation("com.taskrunna:taskrunna:1.1.0")  // Single dependency
}
```

All functionality remains the same - just import paths changed.

---

Need more details? [Check the source code](https://github.com/thisKK/taskrunna-framework) or [open an issue](https://github.com/thisKK/taskrunna-framework/issues)!

</div> 