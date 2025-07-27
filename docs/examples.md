---
layout: default
title: Examples
permalink: /examples/
---

<div class="hero">
  <h1>💡 TaskRunna Examples</h1>
  <p>Real-world examples showing how to use TaskRunna in different scenarios</p>
</div>

<div class="wrapper">
  <div class="intro-text">
    Explore practical implementations of TaskRunna across various use cases, from e-commerce order processing to data pipeline automation.
  </div>

## 🚀 Live Example: Order Retry System

TaskRunna includes a complete working example that demonstrates Prometheus metrics integration.

### Run the Example

<div class="highlight">
<pre><code class="language-bash">git clone https://github.com/thisKK/taskrunna-framework.git
cd taskrunna-framework
./gradlew :taskrunna-examples:run

# Visit http://localhost:8080 for web interface
# Visit http://localhost:8080/metrics for Prometheus metrics</code></pre>
</div>

The example simulates an order retry system processing 50 orders with realistic success/failure patterns.

**Features demonstrated:**
- ✅ HTTP Server with metrics endpoint (Ktor-based)
- ✅ Realistic Batch Processing - Order retry simulation with ~20% failure rate  
- ✅ Live Metrics - Real-time Prometheus metrics collection
- ✅ Multi-threaded Execution - Concurrent task processing
- ✅ Production Patterns - Error handling, logging, observability

## 📊 Example Use Cases

### 1. E-commerce Order Processing

Process failed orders for retry:

```kotlin
data class Order(val id: String, val customerId: String, val amount: Double)

class FailedOrderIterator(private val orderRepo: OrderRepository) : BaseBatchIterator<Order>() {
    override fun loadNextBatch(afterCursor: String, batchSize: Int): List<Order> {
        return orderRepo.findFailedOrders(
            afterId = afterCursor.takeIf { it.isNotEmpty() },
            limit = batchSize
        )
    }
    
    override fun extractCursorFrom(item: Order) = item.id
}

fun retryPayment(order: Order): ListenableFuture<String> {
    return paymentService.retryPaymentAsync(order)
        .transform({ "Successfully processed order ${order.id}" }, directExecutor())
}

// Usage
val processor = BatchJobProcessor(
    iterator = FailedOrderIterator(orderRepository),
    submitJob = ::retryPayment,
    onSuccess = { order, _ -> orderRepository.markAsRetried(order.id) },
    onFailure = { order, error -> deadLetterQueue.send(order, error) }
)
```

### 2. User Email Campaigns

Send marketing emails to user segments:

```kotlin
data class User(val id: String, val email: String, val segment: String)

class EmailCampaignIterator(
    private val userRepo: UserRepository,
    private val segment: String
) : BaseBatchIterator<User>() {
    
    override fun loadNextBatch(afterCursor: String, batchSize: Int): List<User> {
        return userRepo.findUsersBySegment(
            segment = segment,
            afterId = afterCursor.takeIf { it.isNotEmpty() },
            limit = batchSize
        )
    }
    
    override fun extractCursorFrom(item: User) = item.id
}

fun sendEmail(user: User): ListenableFuture<String> {
    return emailService.sendMarketingEmailAsync(
        to = user.email,
        template = "summer_sale_2024",
        userId = user.id
    )
}

// Usage with metrics
val metrics = PrometheusConfig.createBatchMetrics("email_campaign")

val processor = BatchJobProcessor(
    iterator = EmailCampaignIterator(userRepository, "premium_users"),
    submitJob = ::sendEmail,
    onSuccess = { user, _ -> 
        logger.info { "Email sent to ${user.email}" }
        analyticsService.trackEmailSent(user.id)
    },
    onFailure = { user, error -> 
        logger.warn { "Failed to send email to ${user.email}: ${error.message}" }
        bounceListService.addIfDeliveryFailed(user.email, error)
    },
    metrics = metrics,
    jobName = "summer_sale_campaign"
)
```

### 3. Data Pipeline ETL

Extract, transform, and load data:

```kotlin
data class RawEvent(val id: String, val data: JsonNode, val timestamp: Instant)
data class ProcessedEvent(val id: String, val userId: String, val eventType: String, val value: Double)

class EventETLIterator(private val eventRepo: EventRepository) : BaseBatchIterator<RawEvent>() {
    override fun loadNextBatch(afterCursor: String, batchSize: Int): List<RawEvent> {
        return eventRepo.findUnprocessedEvents(
            afterTimestamp = afterCursor.takeIf { it.isNotEmpty() }?.let { Instant.parse(it) },
            limit = batchSize
        )
    }
    
    override fun extractCursorFrom(item: RawEvent) = item.timestamp.toString()
}

fun processEvent(rawEvent: RawEvent): ListenableFuture<ProcessedEvent> {
    return CompletableFuture.supplyAsync {
        // Transform the raw event
        val processed = ProcessedEvent(
            id = rawEvent.id,
            userId = rawEvent.data["user_id"].asText(),
            eventType = rawEvent.data["type"].asText(),
            value = rawEvent.data["value"].asDouble()
        )
        
        // Store in data warehouse
        dataWarehouse.store(processed)
        
        processed
    }.toListenableFuture()
}

// Usage
val processor = BatchJobProcessor(
    iterator = EventETLIterator(eventRepository),
    submitJob = ::processEvent,
    onSuccess = { rawEvent, processed -> 
        eventRepository.markAsProcessed(rawEvent.id)
        metricsCollector.incrementProcessed(processed.eventType)
    },
    onFailure = { rawEvent, error -> 
        eventRepository.markAsFailed(rawEvent.id, error.message)
        alertService.notifyETLFailure(rawEvent.id, error)
    }
)
```

### 4. File Processing

Process uploaded files:

```kotlin
data class UploadedFile(val id: String, val path: String, val type: String, val userId: String)

class FileProcessingIterator(private val fileRepo: FileRepository) : BaseBatchIterator<UploadedFile>() {
    override fun loadNextBatch(afterCursor: String, batchSize: Int): List<UploadedFile> {
        return fileRepo.findPendingFiles(
            afterId = afterCursor.takeIf { it.isNotEmpty() },
            limit = batchSize
        )
    }
    
    override fun extractCursorFrom(item: UploadedFile) = item.id
}

fun processFile(file: UploadedFile): ListenableFuture<String> {
    return CompletableFuture.supplyAsync {
        when (file.type) {
            "image" -> {
                imageProcessor.generateThumbnails(file.path)
                imageProcessor.extractMetadata(file.path)
                "Generated thumbnails and extracted metadata"
            }
            "video" -> {
                videoProcessor.encodeForStreaming(file.path)
                "Encoded video for streaming"
            }
            "document" -> {
                documentProcessor.extractText(file.path)
                documentProcessor.generatePreview(file.path)
                "Extracted text and generated preview"
            }
            else -> throw IllegalArgumentException("Unsupported file type: ${file.type}")
        }
    }.toListenableFuture()
}

// Usage
val processor = BatchJobProcessor(
    iterator = FileProcessingIterator(fileRepository),
    submitJob = ::processFile,
    onSuccess = { file, result -> 
        fileRepository.markAsProcessed(file.id)
        notificationService.notifyUserFileReady(file.userId, file.id)
        logger.info { "Processed file ${file.id}: $result" }
    },
    onFailure = { file, error -> 
        fileRepository.markAsFailed(file.id, error.message)
        notificationService.notifyUserFileError(file.userId, file.id, error.message)
        logger.error(error) { "Failed to process file ${file.id}" }
    }
)
```

### 5. Kafka Message Processing

Process messages from Kafka with TaskRunna:

```kotlin
data class KafkaMessage(val key: String, val value: String, val partition: Int, val offset: Long)

class KafkaMessageIterator(
    private val consumer: KafkaConsumer<String, String>,
    private val topic: String
) : BaseBatchIterator<KafkaMessage>() {
    
    override fun loadNextBatch(afterCursor: String, batchSize: Int): List<KafkaMessage> {
        consumer.poll(Duration.ofSeconds(1)).map { record ->
            KafkaMessage(
                key = record.key(),
                value = record.value(),
                partition = record.partition(),
                offset = record.offset()
            )
        }.take(batchSize).toList()
    }
    
    override fun extractCursorFrom(item: KafkaMessage) = "${item.partition}:${item.offset}"
}

fun processMessage(message: KafkaMessage): ListenableFuture<String> {
    return messageProcessor.processAsync(message.value)
        .transform({ result -> 
            "Processed message from partition ${message.partition}, offset ${message.offset}: $result"
        }, directExecutor())
}

// Usage
val processor = BatchJobProcessor(
    iterator = KafkaMessageIterator(kafkaConsumer, "user-events"),
    submitJob = ::processMessage,
    onSuccess = { message, _ -> 
        // Commit offset after successful processing
        kafkaConsumer.commitSync(mapOf(
            TopicPartition("user-events", message.partition) to OffsetAndMetadata(message.offset + 1)
        ))
    },
    onFailure = { message, error -> 
        // Send to dead letter topic
        deadLetterProducer.send(ProducerRecord("user-events-dlq", message.key, message.value))
        logger.error(error) { "Failed to process message ${message.key}" }
    }
)
```

## 🔧 Configuration Patterns

### With Spring Boot Auto-Configuration

```kotlin
@Configuration
@EnableConfigurationProperties(BatchJobProperties::class)
class BatchJobConfig {
    
    @Bean
    fun orderRetryProcessor(
        orderRepository: OrderRepository,
        paymentService: PaymentService,
        properties: BatchJobProperties
    ): BatchJobProcessor<Order> {
        return BatchJobProcessor(
            iterator = FailedOrderIterator(orderRepository),
            submitJob = { order -> paymentService.retryPaymentAsync(order) },
            onSuccess = { order, _ -> orderRepository.markAsRetried(order.id) },
            onFailure = { order, error -> 
                if (properties.enableDeadLetterQueue) {
                    deadLetterQueue.send(order, error)
                }
            },
            metrics = if (properties.enableMetrics) {
                PrometheusConfig.createBatchMetrics("order_retry")
            } else {
                NoOpBatchMetrics.INSTANCE
            }
        )
    }
}

@ConfigurationProperties(prefix = "batch.job")
data class BatchJobProperties(
    val enableMetrics: Boolean = true,
    val enableDeadLetterQueue: Boolean = true,
    val batchSize: Int = 20
)
```

### Environment-Specific Configuration

```kotlin
class ConfigurableProcessor {
    companion object {
        fun createOrderProcessor(env: Environment): BatchJobProcessor<Order> {
            val metrics = when (env) {
                Environment.PRODUCTION -> PrometheusConfig.createBatchMetrics("order_retry")
                Environment.STAGING -> PrometheusConfig.createBatchMetrics("order_retry_staging")
                Environment.DEVELOPMENT -> NoOpBatchMetrics.INSTANCE
            }
            
            return BatchJobProcessor(
                iterator = FailedOrderIterator(orderRepository),
                submitJob = ::retryPayment,
                onSuccess = { order, _ -> orderRepository.markAsRetried(order.id) },
                onFailure = { order, error -> 
                    if (env.isProd()) {
                        alertService.notifyFailure(order.id, error)
                    }
                    logger.warn { "Failed to process order ${order.id}: ${error.message}" }
                },
                metrics = metrics,
                jobName = "order_retry_${env.name.lowercase()}"
            )
        }
    }
}
```

## 🧪 Testing Examples

### Unit Testing Your Iterator

```kotlin
class OrderIteratorTest {
    
    @Test
    fun `should load orders in batches`() {
        // Given
        val mockRepo = mockk<OrderRepository>()
        every { mockRepo.findFailedOrders(null, 10) } returns listOf(
            Order("1", "customer1", 100.0),
            Order("2", "customer2", 200.0)
        )
        
        val iterator = FailedOrderIterator(mockRepo)
        
        // When
        val batch = iterator.loadNextBatch("", 10)
        
        // Then
        assertThat(batch).hasSize(2)
        assertThat(batch[0].id).isEqualTo("1")
        assertThat(batch[1].id).isEqualTo("2")
    }
    
    @Test
    fun `should return empty list when no more data`() {
        // Given
        val mockRepo = mockk<OrderRepository>()
        every { mockRepo.findFailedOrders("999", 10) } returns emptyList()
        
        val iterator = FailedOrderIterator(mockRepo)
        
        // When
        val batch = iterator.loadNextBatch("999", 10)
        
        // Then
        assertThat(batch).isEmpty()
    }
}
```

### Integration Testing

```kotlin
@SpringBootTest
class BatchJobIntegrationTest {
    
    @Autowired
    private lateinit var orderRepository: OrderRepository
    
    @Autowired
    private lateinit var paymentService: PaymentService
    
    @Test
    fun `should process failed orders successfully`() {
        // Given
        orderRepository.saveAll(listOf(
            Order("1", "customer1", 100.0, OrderStatus.FAILED),
            Order("2", "customer2", 200.0, OrderStatus.FAILED)
        ))
        
        val processor = BatchJobProcessor(
            iterator = FailedOrderIterator(orderRepository),
            submitJob = { order -> paymentService.retryPaymentAsync(order) },
            onSuccess = { order, _ -> orderRepository.markAsRetried(order.id) }
        )
        
        // When
        processor.run()
        
        // Then
        val retriedOrders = orderRepository.findByStatus(OrderStatus.RETRIED)
        assertThat(retriedOrders).hasSize(2)
    }
}
```

Need more examples? [Check the source code](https://github.com/thisKK/taskrunna-framework/tree/main/taskrunna-examples) or [open an issue](https://github.com/thisKK/taskrunna-framework/issues) with your use case!
</div> 