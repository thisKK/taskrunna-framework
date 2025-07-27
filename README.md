# TaskRunna Framework 🏃‍♂️

A **lightweight, single-package** job orchestration framework for asynchronous task execution in microservices. Process batches efficiently with built-in **Prometheus metrics**, error handling, and pagination support.

## ✨ Why TaskRunna?

- 🎯 **Single Dependency** - Just `com.taskrunna:taskrunna` - no complex module management
- 🚀 **Async by Design** - `ListenableFuture`/`CompletableFuture` with non-blocking execution
- 📊 **Production Metrics** - Built-in Prometheus integration for observability
- 🔄 **Smart Batch Processing** - Handles pagination, retries, and graceful shutdowns
- 🛠️ **Plug & Play** - Minimal setup, maximum functionality
- ⚡ **High Performance** - Multi-threaded execution without blocking main pools

## 🆕 v1.1.0 - Simplified!

**Major improvement**: Consolidated from 2 packages into 1 for much simpler usage!

- ✅ **Before**: `taskrunna-core` + `taskrunna-batch` (complex)
- ✅ **Now**: Just `taskrunna` (simple!)
- 🎯 **One import, everything included**

## 📦 What's Included

**`com.taskrunna:taskrunna`** - Complete framework in one package:

- **`BatchJobProcessor`** - Main processing engine with async execution
- **`BaseBatchIterator`** - Abstract pagination iterator for data sources
- **`BatchJobStats`** - Execution statistics and monitoring
- **`BatchMetrics`** - Prometheus metrics integration (optional)
- **`PrometheusConfig`** - Easy metrics setup utilities

**`taskrunna-examples`** - Working examples and integration guides

## 🚀 Quick Start

### Try It Now

Clone and run the working Prometheus metrics example:

```bash
git clone https://github.com/thisKK/taskrunna-framework.git
cd taskrunna-framework
./gradlew :taskrunna-examples:run

# Visit http://localhost:8080 for the web interface
# Visit http://localhost:8080/metrics for Prometheus metrics
```

### Installation

**Simple!** Just add one dependency:

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
    implementation("com.taskrunna:taskrunna:1.1.0") // Everything included!
}
```

> **🔐 Authentication**: GitHub Packages requires a [Personal Access Token](https://github.com/settings/tokens) with `read:packages` permission.

<details>
<summary><strong>📋 Alternative: Build from Source</strong></summary>

```bash
git clone https://github.com/thisKK/taskrunna-framework.git
cd taskrunna-framework
./gradlew publishToMavenLocal

# Then use in your project:
dependencies {
    implementation("com.taskrunna:taskrunna:1.1.0")
}
```
</details>

### Basic Usage

**Process orders from database → Send to Kafka:**

```kotlin
// Single import - everything included! 
import com.taskrunna.batch.*

// 1. Define your data iterator
class OrderIterator : BaseBatchIterator<Order>() {
    override fun loadNextBatch(cursor: String, size: Int) = 
        orderRepository.findPendingOrders(cursor, size)
    
    override fun extractCursorFrom(order: Order) = order.id
}

// 2. Process with async jobs
val processor = BatchJobProcessor(
    iterator = OrderIterator(),
    submitJob = { order -> sendToKafka(order) },    // Returns ListenableFuture
    onSuccess = { order, result -> markProcessed(order.id) },
    onFailure = { order, error -> handleError(order, error) }
)

processor.run() // Processes all orders asynchronously!
```

### With Production Metrics 📊

```kotlin
import com.taskrunna.batch.metrics.PrometheusConfig

// Enable Prometheus metrics (optional)
val metrics = PrometheusConfig.createBatchMetrics("order_processor")

val processor = BatchJobProcessor(
    iterator = OrderIterator(),
    submitJob = { order -> sendToKafka(order) },
    metrics = metrics,  // Automatic observability!
    jobName = "order_processing"
)

processor.run()

// Metrics automatically available at /metrics endpoint!
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

### Live Example

Run the included example to see these metrics in action:

```bash
./gradlew :taskrunna-examples:run
curl http://localhost:8080/metrics | grep order_retry
```

The example simulates an order retry system processing 50 orders with realistic success/failure patterns.

### Example Features

The included `PrometheusMetricsExample` demonstrates:

- ✅ **HTTP Server** with metrics endpoint (Ktor-based)
- ✅ **Realistic Batch Processing** - Order retry simulation with ~20% failure rate  
- ✅ **Live Metrics** - Real-time Prometheus metrics collection
- ✅ **Multi-threaded Execution** - Concurrent task processing
- ✅ **Production Patterns** - Error handling, logging, observability

**Available Endpoints:**
- `GET /` - Example information and status
- `GET /metrics` - Prometheus metrics (ready for scraping)
- `GET /health` - Health check endpoint

## 🏗️ Project Structure

**Simple & Clean** - Just what you need:

```
taskrunna-framework/
├── taskrunna/                    # 📦 Single Package - Everything included
│   ├── BatchJobProcessor         #   🏗️  Main async processing engine  
│   ├── BaseBatchIterator         #   🔄  Pagination & data iteration
│   ├── BatchJobStats             #   📊  Execution statistics
│   └── metrics/
│       ├── BatchMetrics          #   📈  Metrics interface
│       ├── MicrometerBatchMetrics#   🔗  Prometheus integration  
│       └── PrometheusConfig      #   ⚙️   Easy setup utilities
└── taskrunna-examples/           # 🎯 Working Examples
    ├── SimpleExample             #   📝  Basic usage demo
    └── PrometheusMetricsExample  #   🚀  Production-ready example
```

**v1.1.0 Benefits**: Single import, everything works together seamlessly!

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

### Example Usage

```bash
# Run the Prometheus metrics example
./gradlew :taskrunna-examples:run

# In another terminal, monitor metrics in real-time
watch -n 1 "curl -s http://localhost:8080/metrics | grep order_retry"

# Or view specific metrics
curl http://localhost:8080/metrics | grep -E "(tasks_submitted|job_duration)"
```

## 📚 Documentation

- **[PUBLISHING.md](PUBLISHING.md)** - How to publish to GitHub Packages & Maven Central
- **[METRICS.md](METRICS.md)** - Comprehensive Prometheus metrics guide
- **[CONTRIBUTING.md](CONTRIBUTING.md)** - Development setup and guidelines  
- **[DEVBOX.md](DEVBOX.md)** - Devbox environment quick reference

## 🎯 Perfect For

- **Microservices** with batch processing needs
- **Data pipelines** requiring async execution
- **Systems** needing production-ready observability
- **Teams** who want simple, powerful tools

---

**TaskRunna v1.1.0** - One package, endless possibilities! 🚀

[![GitHub](https://img.shields.io/badge/GitHub-thisKK%2Ftaskrunna--framework-blue?logo=github)](https://github.com/thisKK/taskrunna-framework)
[![Packages](https://img.shields.io/badge/Packages-GitHub-green?logo=github)](https://github.com/thisKK/taskrunna-framework/packages) 