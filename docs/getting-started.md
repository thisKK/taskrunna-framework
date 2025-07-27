---
layout: default
title: Getting Started
permalink: /getting-started/
---

# 🚀 Getting Started with TaskRunna

This guide will walk you through creating your first TaskRunna batch processor in just a few minutes.

## Prerequisites

- **Java 17+** or **Kotlin 1.9.20+**
- **Gradle 8.0+** or **Maven 3.6+**
- Basic understanding of asynchronous programming

## Step 1: Add TaskRunna to Your Project

### Gradle (Recommended)

```kotlin
dependencies {
    implementation("com.taskrunna:taskrunna:1.1.0")
}
```

### Maven

```xml
<dependency>
    <groupId>com.taskrunna</groupId>
    <artifactId>taskrunna</artifactId>
    <version>1.1.0</version>
</dependency>
```

## Step 2: Create Your Data Model

First, define the data you want to process:

```kotlin
data class Order(
    val id: String,
    val customerId: String,
    val amount: Double,
    val status: String
)
```

## Step 3: Implement Your Batch Iterator

Create an iterator that loads data in batches:

```kotlin
import com.taskrunna.batch.BaseBatchIterator

class OrderIterator(private val orderService: OrderService) : BaseBatchIterator<Order>() {
    
    override fun loadNextBatch(afterCursor: String, batchSize: Int): List<Order> {
        // Load orders from your data source
        return orderService.findFailedOrders(
            afterId = afterCursor.takeIf { it.isNotEmpty() },
            limit = batchSize
        )
    }
    
    override fun extractCursorFrom(item: Order): String {
        // Return a unique identifier for pagination
        return item.id
    }
}
```

## Step 4: Define Your Job Processing Logic

Create the async job that processes each item:

```kotlin
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture

fun retryOrder(order: Order): ListenableFuture<String> {
    val future = SettableFuture.create<String>()
    
    // Simulate async processing (e.g., HTTP call, message queue)
    CompletableFuture.supplyAsync {
        try {
            // Your business logic here
            paymentService.retryPayment(order)
            orderService.markAsRetried(order.id)
            "Successfully retried order ${order.id}"
        } catch (e: Exception) {
            throw RuntimeException("Failed to retry order ${order.id}: ${e.message}", e)
        }
    }.whenComplete { result, error ->
        if (error != null) {
            future.setException(error)
        } else {
            future.set(result)
        }
    }
    
    return future
}
```

## Step 5: Create and Run the Batch Processor

Put it all together:

```kotlin
import com.taskrunna.batch.BatchJobProcessor
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

fun main() {
    // Create the batch processor
    val processor = BatchJobProcessor(
        iterator = OrderIterator(orderService),
        submitJob = ::retryOrder,
        onSuccess = { order, result -> 
            logger.info { "✅ $result" }
        },
        onFailure = { order, error -> 
            logger.error(error) { "❌ Failed to process order ${order.id}" }
            // Optional: Send to dead letter queue, alert, etc.
        },
        logger = logger
    )
    
    // Run the batch job
    logger.info { "Starting order retry batch job..." }
    processor.run()
    logger.info { "Batch job completed!" }
}
```

## Step 6: Run Your Application

```bash
./gradlew run
```

You should see output like:

```
[main] INFO  OrderRetryJob - Starting order retry batch job...
[main] INFO  OrderRetryJob - [BatchJob] Starting...
[main] INFO  OrderRetryJob - [BatchJob] Retrieved 25 record(s).
Batch #1 - 25 records in 0.045 sec.
[pool-1-thread-1] INFO  OrderRetryJob - ✅ Successfully retried order ORD-001
[pool-1-thread-2] INFO  OrderRetryJob - ✅ Successfully retried order ORD-002
...
[main] INFO  OrderRetryJob - Completed processing 25 records — 23 succeeded (92.00%) in 1.23 seconds. 2 records failed
[main] INFO  OrderRetryJob - [BatchJob] All jobs submitted. Job completed.
[main] INFO  OrderRetryJob - Batch job completed!
```

## 🎉 Congratulations!

You've successfully created your first TaskRunna batch processor! 

## Next Steps

- **Add Metrics**: [Learn how to add Prometheus metrics](metrics)
- **See More Examples**: [Explore real-world use cases](examples)
- **Production Setup**: [Best practices for production deployments](api-reference#production-configuration)

## Configuration Options

### Batch Size

Control how many items are loaded per batch:

```kotlin
class OrderIterator : BaseBatchIterator<Order>() {
    override val defaultBatchSize = 50 // Default: 20
    
    // ... rest of implementation
}
```

### Thread Pool Configuration

TaskRunna uses a default thread pool, but you can customize it:

```kotlin
val processor = BatchJobProcessor(
    iterator = OrderIterator(orderService),
    submitJob = ::retryOrder,
    // Custom configuration via constructor parameters
    logger = logger
)
```

### Error Handling Strategies

```kotlin
val processor = BatchJobProcessor(
    iterator = OrderIterator(orderService),
    submitJob = ::retryOrder,
    onSuccess = { order, result -> 
        logger.info { "Processed: ${order.id}" }
        metricsCollector.incrementSuccess()
    },
    onFailure = { order, error -> 
        logger.error(error) { "Failed: ${order.id}" }
        
        // Different strategies based on error type
        when (error) {
            is TimeoutException -> deadLetterQueue.send(order)
            is ValidationException -> invalidOrderQueue.send(order)
            else -> alertService.notify("Unexpected error processing ${order.id}")
        }
        
        metricsCollector.incrementFailure(error::class.simpleName)
    },
    logger = logger
)
```

## Troubleshooting

### Common Issues

**Q: My batch processor stops after the first batch**
**A:** Make sure your `loadNextBatch` method returns an empty list when there's no more data:

```kotlin
override fun loadNextBatch(afterCursor: String, batchSize: Int): List<Order> {
    val orders = orderService.findFailedOrders(afterCursor, batchSize)
    return orders // Return empty list when no more data
}
```

**Q: I'm getting "Unresolved reference" errors**
**A:** Make sure you have the required dependencies:

```kotlin
dependencies {
    implementation("com.taskrunna:taskrunna:1.1.0")
    implementation("com.google.guava:guava:32.1.3-jre") // For ListenableFuture
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0") // For logging
}
```

**Q: How do I handle database transactions?**
**A:** Handle transactions in your job processing function:

```kotlin
fun retryOrder(order: Order): ListenableFuture<String> {
    return CompletableFuture.supplyAsync {
        transactionManager.executeInTransaction {
            // Your transactional logic here
            orderService.retryPayment(order)
        }
    }.toListenableFuture()
}
```

Need more help? [Open an issue on GitHub](https://github.com/thisKK/taskrunna-framework/issues)! 