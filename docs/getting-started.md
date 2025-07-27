---
layout: default
title: Getting Started
description: Step-by-step guide to building your first TaskRunna batch processor
---

<div class="hero">
  <h1>🚀 Getting Started with TaskRunna</h1>
  <p>Build your first async batch processor in minutes</p>
</div>

Welcome to TaskRunna! This guide will walk you through creating your first batch processing application using TaskRunna Framework.

## 📋 Prerequisites

- **Java 17+** (OpenJDK recommended)
- **Gradle 7.4+** or **Maven 3.6+**
- **Kotlin 1.9.20+** (or Java 8+ if using Java)

<div class="quick-start">
  <h2>⚡ Quick Setup</h2>
  
  <h3>1. Add TaskRunna Dependency</h3>
  
  **Gradle (Kotlin DSL):**
  ```kotlin
  repositories {
      maven {
          url = uri("https://maven.pkg.github.com/thisKK/taskrunna-framework")
          credentials {
              username = "your-github-username"
              password = "your-github-token" // Personal access token with read:packages
          }
      }
  }
  
  dependencies {
      implementation("com.taskrunna:taskrunna:1.1.0")
  }
  ```
  
  **Maven:**
  ```xml
  <repositories>
      <repository>
          <id>github</id>
          <url>https://maven.pkg.github.com/thisKK/taskrunna-framework</url>
      </repository>
  </repositories>
  
  <dependencies>
      <dependency>
          <groupId>com.taskrunna</groupId>
          <artifactId>taskrunna</artifactId>
          <version>1.1.0</version>
      </dependency>
  </dependencies>
  ```
  
  > 🔐 **Authentication Required**: GitHub Packages needs a [Personal Access Token](https://github.com/settings/tokens) with `read:packages` permission.
</div>

## 🎯 Your First Batch Processor

Let's create a simple batch processor that processes customer orders:

### Step 1: Define Your Data Model

```kotlin
data class Order(
    val id: String,
    val customerId: String,
    val amount: Double,
    val status: String
)
```

### Step 2: Create a Batch Iterator

```kotlin
import com.taskrunna.batch.BaseBatchIterator

class OrderIterator(private val repository: OrderRepository) : BaseBatchIterator<Order>() {
    
    override fun loadNextBatch(afterCursor: String, batchSize: Int): List<Order> {
        return repository.findPendingOrders(afterCursor, batchSize)
    }
    
    override fun extractCursorFrom(item: Order): String {
        return item.id
    }
}
```

### Step 3: Create Your Batch Processor

```kotlin
import com.taskrunna.batch.BatchJobProcessor
import com.google.common.util.concurrent.ListenableFuture
import io.github.oshai.kotlinlogging.KotlinLogging

class OrderProcessor {
    private val logger = KotlinLogging.logger {}
    
    fun processOrders() {
        val processor = BatchJobProcessor(
            iterator = OrderIterator(orderRepository),
            submitJob = { order -> processOrder(order) },
            onSuccess = { order, result -> 
                logger.info { "Successfully processed order ${order.id}" }
                markOrderComplete(order.id)
            },
            onFailure = { order, error -> 
                logger.error(error) { "Failed to process order ${order.id}" }
                markOrderFailed(order.id, error.message)
            },
            logger = logger
        )
        
        processor.run()
    }
    
    private fun processOrder(order: Order): ListenableFuture<String> {
        // Your async processing logic here
        // e.g., send to Kafka, call external API, etc.
        return someAsyncService.process(order)
    }
}
```

### Step 4: Run Your Processor

```kotlin
fun main() {
    val orderProcessor = OrderProcessor()
    orderProcessor.processOrders()
}
```

## 🎛️ Configuration Options

TaskRunna provides flexible configuration:

### Batch Size Configuration

```kotlin
val processor = BatchJobProcessor(
    iterator = OrderIterator(repository),
    submitJob = { order -> processOrder(order) },
    batchSize = 50,  // Process 50 items at a time
    maxConcurrency = 10,  // Max 10 concurrent jobs
    // ... other options
)
```

### Thread Pool Configuration

```kotlin
val customExecutor = Executors.newFixedThreadPool(20)

val processor = BatchJobProcessor(
    iterator = OrderIterator(repository),
    submitJob = { order -> processOrder(order) },
    executor = customExecutor,  // Use custom thread pool
    // ... other options
)
```

### Error Handling

```kotlin
val processor = BatchJobProcessor(
    iterator = OrderIterator(repository),
    submitJob = { order -> processOrder(order) },
    onSuccess = { order, result -> 
        // Handle successful processing
        updateOrderStatus(order.id, "COMPLETED", result)
    },
    onFailure = { order, error -> 
        // Handle processing errors
        updateOrderStatus(order.id, "FAILED", error.message)
        
        // Optional: send to dead letter queue
        deadLetterQueue.send(order, error)
    }
)
```

## 📊 Adding Metrics (Optional)

For production monitoring, add Prometheus metrics:

```kotlin
import com.taskrunna.batch.metrics.PrometheusConfig

// Create metrics registry
val metrics = PrometheusConfig.createBatchMetrics("order_processor")

val processor = BatchJobProcessor(
    iterator = OrderIterator(repository),
    submitJob = { order -> processOrder(order) },
    metrics = metrics,  // Enable metrics collection
    jobName = "order_processing",
    // ... other options
)
```

See our [Metrics Guide](metrics) for complete monitoring setup.

## ✅ Testing Your Processor

### Unit Testing

```kotlin
import org.junit.jupiter.api.Test
import io.mockk.mockk
import io.mockk.every

class OrderProcessorTest {
    
    @Test
    fun `should process orders successfully`() {
        // Given
        val mockRepository = mockk<OrderRepository>()
        val orders = listOf(
            Order("1", "customer1", 100.0, "PENDING"),
            Order("2", "customer2", 200.0, "PENDING")
        )
        
        every { mockRepository.findPendingOrders(any(), any()) } returns orders
        
        // When
        val iterator = OrderIterator(mockRepository)
        val batch = iterator.loadNextBatch("", 10)
        
        // Then
        assertEquals(2, batch.size)
        assertEquals("1", iterator.extractCursorFrom(batch[0]))
    }
}
```

## 🚀 Next Steps

1. **[Explore Examples](examples)** - See real-world use cases
2. **[Add Monitoring](metrics)** - Set up Prometheus metrics  
3. **[API Reference](api-reference)** - Deep dive into all features

## 🆘 Troubleshooting

### Common Issues

**Authentication Error with GitHub Packages:**
```
Could not resolve com.taskrunna:taskrunna:1.1.0
```
**Solution:** Ensure your GitHub token has `read:packages` permission and is correctly configured.

**OutOfMemoryError:**
```
java.lang.OutOfMemoryError: GC overhead limit exceeded
```
**Solution:** Reduce `batchSize` or increase JVM heap space with `-Xmx2g`.

**Too Many Concurrent Requests:**
```
Connection pool exhausted
```
**Solution:** Reduce `maxConcurrency` parameter.

### Getting Help

- 📖 [API Reference](api-reference) - Complete documentation
- 🐛 [GitHub Issues](https://github.com/thisKK/taskrunna-framework/issues) - Report bugs
- 💡 [Examples](examples) - More code samples

Ready to build something awesome? Let's go! 🚀 