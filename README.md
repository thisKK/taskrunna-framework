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

```kotlin
val processor = BatchJobProcessor(
    iterator = MyBatchIterator(repo),
    submitJob = { item -> sendToKafka(item) },
    onSuccess = { item, result -> markDone(item.id) },
    onFailure = { item, error -> log.warn("fail: ${item.id}") }
)
processor.run()

``` 