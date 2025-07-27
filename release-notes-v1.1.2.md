# 🚀 TaskRunna Framework v1.1.2: Major Dependency Updates & Performance Improvements

**All dependencies updated to latest stable versions with significant performance gains!**

## ✨ Performance Enhancements

- 🚀 **Kotlin 2.2.0** with K2 compiler - **Up to 2x faster compilation!**
- ⚡ **1.8x faster** code highlighting and completion in IDE
- 📊 **Latest Micrometer 1.14.2** - Enhanced observability and metrics
- 🌐 **Ktor 3.1.0** - Major version upgrade with better performance
- 📈 **Latest Guava 33.4.8** - Performance improvements and stability

## 🔧 Major Dependency Updates

| Dependency | Previous | New | Highlights |
|------------|----------|-----|------------|
| **Kotlin** | `1.9.20` | `2.2.0` | K2 compiler, 2x faster builds |
| **Micrometer** | `1.12.0` | `1.14.2` | Enhanced observability |
| **Ktor** | `2.3.6` | `3.1.0` | Major version, better performance |
| **Guava** | `32.1.3-jre` | `33.4.8-jre` | Latest stable, performance gains |
| **ktlint** | `11.6.1` | `12.1.1` | Latest code style enforcement |
| **Dokka** | `1.9.10` | `2.0.0` | V2 migration, future-ready |
| **JUnit 5** | `5.10.0` | `5.11.4` | Latest testing framework |
| **Kotlinx Coroutines** | `1.7.3` | `1.10.1` | Enhanced async support |

## 🛠️ Quality Improvements

- ✅ **Eliminated all deprecation warnings** with Dokka V2 migration
- ✅ **Enhanced build performance** with K2 compiler
- ✅ **Better observability** with latest Micrometer features
- ✅ **Future-ready** with latest stable dependencies
- ✅ **All tests passing** with updated dependencies
- ✅ **ktlint compliance** maintained throughout

## 🔄 Breaking Changes

**None!** This is a **drop-in replacement** - just update your version:

```kotlin
dependencies {
    implementation("com.taskrunna:taskrunna:1.1.2") // 🎯 Updated!
}
```

## 📦 Installation

### GitHub Packages

```kotlin
repositories {
    maven {
        url = uri("https://maven.pkg.github.com/thisKK/taskrunna-framework")
        credentials {
            username = "your-github-username"
            password = "your-github-token"
        }
    }
}

dependencies {
    implementation("com.taskrunna:taskrunna:1.1.2")
}
```

### Build from Source

```bash
git clone https://github.com/thisKK/taskrunna-framework.git
cd taskrunna-framework
./gradlew publishToMavenLocal
```

## 🎯 What's Included

- **BatchJobProcessor** - Main async processing engine with enhanced performance
- **BaseBatchIterator** - Cursor-based pagination for data sources  
- **BatchJobStats** - Execution statistics and monitoring
- **BatchMetrics** - Prometheus metrics integration (enhanced)
- **PrometheusConfig** - Easy metrics setup utilities

## 🔍 Example Usage

```kotlin
import com.taskrunna.batch.*

// Your batch iterator
class OrderRetryIterator : BaseBatchIterator<OrderRetry>() {
    override fun loadNextBatch(cursor: String?, batchSize: Int): List<OrderRetry> {
        return orderService.findFailedOrders(cursor, batchSize)
    }
    override fun extractCursorFrom(item: OrderRetry): String = item.id
}

// Process with metrics
val processor = BatchJobProcessor(
    iterator = OrderRetryIterator(),
    submitJob = { order -> retryOrderAsync(order) },
    onSuccess = { order, result -> logger.info("✅ Retried order ${order.orderId}") },
    onFailure = { order, error -> logger.warn("⚠️ Failed to retry ${order.orderId}") },
    metrics = MicrometerBatchMetrics(prometheusRegistry, "order_retry")
)

processor.run() // 🚀 Enhanced performance with v1.1.2!
```

## 📊 Metrics & Observability

Enhanced Prometheus metrics with latest Micrometer:

- `{prefix}_jobs_started_total` - Batch jobs started
- `{prefix}_jobs_completed_total` - Batch jobs completed  
- `{prefix}_job_duration_seconds` - Job execution time
- `{prefix}_tasks_submitted_total` - Individual tasks submitted
- `{prefix}_tasks_completed_total` - Individual tasks completed
- `{prefix}_task_duration_seconds` - Task execution time

**Try the live example:**
```bash
./gradlew :taskrunna-examples:run
curl http://localhost:8080/metrics | grep order_retry
```

## 🙏 Thanks

Special thanks to all contributors and the open-source community for the amazing libraries that make TaskRunna possible!

---

**Full Changelog**: https://github.com/thisKK/taskrunna-framework/compare/v1.1.1...v1.1.2 