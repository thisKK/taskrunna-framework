---
layout: default
title: TaskRunna Framework
description: Lightweight, single-package job orchestration framework for asynchronous task execution
---

<div class="hero">
  <h1>TaskRunna Framework 🏃‍♂️</h1>
  <p>A lightweight, single-package job orchestration framework for asynchronous task execution in microservices</p>
</div>

<div class="badges">
  <a href="https://github.com/thisKK/taskrunna-framework/releases">
    <img src="https://img.shields.io/github/v/release/thisKK/taskrunna-framework" alt="GitHub release">
  </a>
  <img src="https://img.shields.io/badge/Kotlin-1.9.20-7F52FF.svg?logo=kotlin" alt="Kotlin">
  <img src="https://img.shields.io/badge/JVM-17+-orange.svg" alt="JVM">
  <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License: MIT">
</div>

## ✨ Why TaskRunna?

<div class="features">
  <div class="feature-item">
    <div class="feature-emoji">🎯</div>
    <div class="feature-content">
      <strong>Single Dependency</strong>
      Just `com.taskrunna:taskrunna` - no complex module management
    </div>
  </div>
  
  <div class="feature-item">
    <div class="feature-emoji">🚀</div>
    <div class="feature-content">
      <strong>Async by Design</strong>
      `ListenableFuture`/`CompletableFuture` with non-blocking execution
    </div>
  </div>
  
  <div class="feature-item">
    <div class="feature-emoji">📊</div>
    <div class="feature-content">
      <strong>Production Metrics</strong>
      Built-in Prometheus integration for observability
    </div>
  </div>
  
  <div class="feature-item">
    <div class="feature-emoji">🔄</div>
    <div class="feature-content">
      <strong>Smart Batch Processing</strong>
      Handles pagination, retries, and graceful shutdowns
    </div>
  </div>
  
  <div class="feature-item">
    <div class="feature-emoji">🛠️</div>
    <div class="feature-content">
      <strong>Plug & Play</strong>
      Minimal setup, maximum functionality
    </div>
  </div>
  
  <div class="feature-item">
    <div class="feature-emoji">⚡</div>
    <div class="feature-content">
      <strong>High Performance</strong>
      Multi-threaded execution without blocking main pools
    </div>
  </div>
</div>

<div class="quick-start">
  <h2>🚀 Quick Start</h2>
  
  <h3>Installation</h3>
  
  ```kotlin
  dependencies {
      implementation("com.taskrunna:taskrunna:1.1.0")
  }
  ```
  
  <h3>Basic Usage</h3>
  
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
</div>

## 📚 Documentation

<div class="docs-grid">
  <div class="docs-card">
    <h3><a href="getting-started">🚀 Getting Started</a></h3>
    <p>Step-by-step tutorial to build your first batch processor with TaskRunna</p>
  </div>
  
  <div class="docs-card">
    <h3><a href="examples">💡 Examples</a></h3>
    <p>Real-world examples: order processing, ETL pipelines, email campaigns</p>
  </div>
  
  <div class="docs-card">
    <h3><a href="metrics">📊 Metrics & Monitoring</a></h3>
    <p>Prometheus integration, dashboards, and production observability</p>
  </div>
  
  <div class="docs-card">
    <h3><a href="api-reference">📖 API Reference</a></h3>
    <p>Complete API documentation for all classes and configurations</p>
  </div>
</div>

## 🆕 v1.1.0 - Major Simplification!

**Consolidated from 2 packages into 1** for much simpler usage:

- ✅ **Before**: `taskrunna-core` + `taskrunna-batch` (complex)
- ✅ **Now**: Just `taskrunna` (simple!)  
- 🎯 **One import, everything included**

## 🎯 Perfect For

- **🏢 Microservices** with batch processing needs
- **🔄 Data pipelines** requiring async execution  
- **📊 Systems** needing production-ready observability
- **👥 Teams** who want simple, powerful tools

## 🔗 Useful Links

<div class="links-grid">
  <div class="link-item">
    <a href="https://github.com/thisKK/taskrunna-framework">📂 GitHub Repository</a>
  </div>
  
  <div class="link-item">
    <a href="https://github.com/thisKK/taskrunna-framework/releases">🏷️ Releases</a>
  </div>
  
  <div class="link-item">
    <a href="https://github.com/thisKK/taskrunna-framework/issues">🐛 Issues</a>
  </div>
  
  <div class="link-item">
    <a href="https://github.com/thisKK/taskrunna-framework/blob/main/LICENSE">📄 MIT License</a>
  </div>
</div>

---

**TaskRunna Framework** - Making async batch processing simple and powerful! 🚀 

 