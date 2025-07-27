---
layout: default
title: TaskRunna Framework
description: Lightweight, single-package job orchestration framework for asynchronous task execution
---

# TaskRunna Framework 🏃‍♂️

[![GitHub release](https://img.shields.io/github/v/release/thisKK/taskrunna-framework)](https://github.com/thisKK/taskrunna-framework/releases)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.20-7F52FF.svg?logo=kotlin)](https://kotlinlang.org)
[![JVM](https://img.shields.io/badge/JVM-17+-orange.svg)](https://openjdk.org)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

A **lightweight, single-package** job orchestration framework for asynchronous task execution in microservices. Process batches efficiently with built-in **Prometheus metrics**, error handling, and pagination support.

## 🚀 Quick Start

### Installation

```kotlin
dependencies {
    implementation("com.taskrunna:taskrunna:1.1.0")
}
```

### Basic Usage

```kotlin
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
    submitJob = { order -> sendToKafka(order) },
    onSuccess = { order, result -> markProcessed(order.id) },
    onFailure = { order, error -> handleError(order, error) }
)

processor.run() // Processes all orders asynchronously!
```

## 📚 Documentation

<div class="docs-grid">
  <div class="docs-card">
    <h3><a href="getting-started">🚀 Getting Started</a></h3>
    <p>Step-by-step tutorial to build your first batch processor</p>
  </div>
  
  <div class="docs-card">
    <h3><a href="examples">💡 Examples</a></h3>
    <p>Real-world examples and use cases</p>
  </div>
  
  <div class="docs-card">
    <h3><a href="metrics">📊 Metrics & Monitoring</a></h3>
    <p>Prometheus integration and observability</p>
  </div>
  
  <div class="docs-card">
    <h3><a href="api-reference">📖 API Reference</a></h3>
    <p>Complete API documentation</p>
  </div>
</div>

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

## 🎯 Perfect For

- **Microservices** with batch processing needs
- **Data pipelines** requiring async execution  
- **Systems** needing production-ready observability
- **Teams** who want simple, powerful tools

## 🔗 Links

- [GitHub Repository](https://github.com/thisKK/taskrunna-framework)
- [Releases](https://github.com/thisKK/taskrunna-framework/releases)
- [Issues](https://github.com/thisKK/taskrunna-framework/issues)
- [License](https://github.com/thisKK/taskrunna-framework/blob/main/LICENSE)

<style>
.docs-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 20px;
  margin: 30px 0;
}

.docs-card {
  border: 1px solid #e1e4e8;
  border-radius: 8px;
  padding: 20px;
  background: #f6f8fa;
}

.docs-card h3 {
  margin-top: 0;
  color: #0366d6;
}

.docs-card h3 a {
  text-decoration: none;
  color: inherit;
}

.docs-card h3 a:hover {
  text-decoration: underline;
}

.docs-card p {
  color: #586069;
  margin-bottom: 0;
}
</style> 

 