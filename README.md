# TaskRunna Framework 🏃‍♂️

TaskRunna is a lightweight, modular job orchestration framework designed for asynchronous task execution in microservices.

## ✨ Highlights
- Plug-and-play `TaskRunner` and `BatchProcessor`
- Async job submission (`ListenableFuture` / `CompletableFuture`)
- Built-in hooks: `onSuccess`, `onFailure`, metrics, and observability
- Supports multi-threaded execution without blocking main pools
- Production-ready: minimal setup, maximal clarity

## 📦 Modules

| Module              | Description                                      |
|---------------------|--------------------------------------------------|
| `taskrunna-core`     | Core interfaces, common utilities               |
| `taskrunna-batch`    | Batch job processing module                     |
| `taskrunna-examples` | Sample use cases and integration guides         |

## 🚀 Quick Start

### Installation

Add TaskRunna to your Gradle build:

```kotlin
dependencies {
    implementation("com.taskrunna:taskrunna-batch:1.0.0")
}
```

### Basic Usage

```kotlin
import com.taskrunna.batch.BatchJobProcessor
import com.taskrunna.core.BaseBatchIterator

// 1. Create your batch iterator
class MyBatchIterator(private val repo: Repository) : BaseBatchIterator<MyItem>() {
    override fun loadNextBatch(afterCursor: String, batchSize: Int) = 
        repo.findBatch(afterCursor, batchSize)
    
    override fun extractCursorFrom(item: MyItem) = item.id
}

// 2. Create and run the processor
val processor = BatchJobProcessor(
    iterator = MyBatchIterator(repo),
    submitJob = { item -> sendToKafka(item) },
    onSuccess = { item, result -> markDone(item.id) },
    onFailure = { item, error -> log.warn("fail: ${item.id}") },
    logger = logger
)
processor.run()
```

### With Prometheus Metrics

```kotlin
import com.taskrunna.batch.metrics.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

// Setup Prometheus metrics
val prometheusRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
val metrics = PrometheusConfig.createBatchMetrics(prometheusRegistry, "my_app")

val processor = BatchJobProcessor(
    iterator = MyBatchIterator(repo),
    submitJob = { item -> sendToKafka(item) },
    onSuccess = { item, result -> markDone(item.id) },
    onFailure = { item, error -> log.warn("fail: ${item.id}") },
    logger = logger,
    metrics = metrics,
    jobName = "kafka_publisher"
)
processor.run()

// Expose metrics endpoint
// GET /metrics -> prometheusRegistry.scrape()
```

## 📊 Metrics & Observability

TaskRunna provides comprehensive Prometheus metrics out of the box:

| Metric | Type | Description |
|--------|------|-------------|
| `{prefix}_jobs_started_total` | Counter | Total batch jobs started |
| `{prefix}_jobs_completed_total` | Counter | Total batch jobs completed (success/failure) |
| `{prefix}_job_duration_seconds` | Timer | Time taken for complete jobs |
| `{prefix}_tasks_submitted_total` | Counter | Total tasks submitted for processing |
| `{prefix}_tasks_completed_total` | Counter | Total tasks completed (success/failure) |
| `{prefix}_task_duration_seconds` | Timer | Time taken for individual tasks |
| `{prefix}_batches_processed_total` | Counter | Total batches processed |
| `{prefix}_items_processed_total` | Counter | Total items processed across all batches |

All metrics include relevant tags like `job_name`, `result`, and `error_type` for detailed observability.

## 🏗️ Project Structure

```
taskrunna-framework/
├── taskrunna-core/          # Core interfaces and utilities
│   └── BaseBatchIterator    # Abstract pagination iterator
├── taskrunna-batch/         # Batch processing implementation
│   ├── BatchJobProcessor    # Main processing engine
│   └── BatchJobStats        # Metrics and monitoring
└── taskrunna-examples/      # Usage examples and demos
    └── SimpleExample        # Basic usage demonstration
```

## 🔧 Development

### Quick Start with Devbox

```bash
# Install devbox and enter development environment
curl -fsSL https://get.jetpack.io/devbox | bash
devbox shell

# Setup and build
devbox run setup
devbox run build

# Code quality (optional)
devbox run format  # Auto-format code
devbox run check   # Lint + test
```

See [CONTRIBUTING.md](CONTRIBUTING.md) for complete development guidelines and build instructions. 