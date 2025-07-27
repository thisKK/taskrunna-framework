---
layout: default
title: TaskRunna Framework
description: Lightweight, single-package job orchestration framework for asynchronous task execution
---

<div class="hero">
  <h1>TaskRunna Framework 🏃‍♂️</h1>
  <p>A lightweight, single-package job orchestration framework for asynchronous task execution in microservices</p>
</div>

<div class="wrapper">
  <div class="badges">
    <a href="https://github.com/thisKK/taskrunna-framework/releases">
      <img src="https://img.shields.io/github/v/release/thisKK/taskrunna-framework" alt="GitHub release">
    </a>
    <img src="https://img.shields.io/badge/Kotlin-1.9.20-7F52FF.svg?logo=kotlin" alt="Kotlin">
    <img src="https://img.shields.io/badge/JVM-17+-orange.svg" alt="JVM">
    <img src="https://img.shields.io/badge/License-MIT-yellow.svg" alt="License: MIT">
  </div>

  <h2>✨ Why TaskRunna?</h2>
  
  <div class="features">
    <div class="feature-item">
      <span class="feature-emoji">🎯</span>
      <strong>Single Dependency</strong>
      <p>Just `com.taskrunna:taskrunna` - no complex module management needed</p>
    </div>
    
    <div class="feature-item">
      <span class="feature-emoji">🚀</span>
      <strong>Async by Design</strong>
      <p>`ListenableFuture`/`CompletableFuture` with non-blocking execution</p>
    </div>
    
    <div class="feature-item">
      <span class="feature-emoji">📊</span>
      <strong>Production Metrics</strong>
      <p>Built-in Prometheus integration for complete observability</p>
    </div>
    
    <div class="feature-item">
      <span class="feature-emoji">🔄</span>
      <strong>Smart Batch Processing</strong>
      <p>Handles pagination, retries, and graceful shutdowns automatically</p>
    </div>
    
    <div class="feature-item">
      <span class="feature-emoji">🛠️</span>
      <strong>Plug & Play</strong>
      <p>Minimal setup, maximum functionality - get started in minutes</p>
    </div>
    
    <div class="feature-item">
      <span class="feature-emoji">⚡</span>
      <strong>High Performance</strong>
      <p>Multi-threaded execution without blocking main pools</p>
    </div>
  </div>

  <div class="quick-start">
    <h2>🚀 Quick Start</h2>
    
    <h3>Installation</h3>

{% highlight kotlin %}
dependencies {
    implementation("com.taskrunna:taskrunna:1.1.0")
}
{% endhighlight %}
    
    <h3>Basic Usage</h3>

{% highlight kotlin %}
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
{% endhighlight %}
  </div>

  <div class="version-highlight">
    <h2>🆕 v1.1.0 - Major Simplification!</h2>
    <p><strong>Consolidated from 2 packages into 1</strong> for much simpler usage:</p>
    <p>✅ <strong>Before:</strong> `taskrunna-core` + `taskrunna-batch` (complex)</p>
    <p>✅ <strong>Now:</strong> Just `taskrunna` (simple!)</p>
    <p>🎯 <strong>One import, everything included</strong></p>
  </div>

  <h2>📚 Documentation</h2>

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

  <div class="perfect-for">
    <h2>🎯 Perfect For</h2>
    <ul>
      <li>🏢 <strong>Microservices</strong> with batch processing needs</li>
      <li>🔄 <strong>Data pipelines</strong> requiring async execution</li>
      <li>📊 <strong>Systems</strong> needing production-ready observability</li>
      <li>👥 <strong>Teams</strong> who want simple, powerful tools</li>
    </ul>
  </div>

  <h2>🔗 Useful Links</h2>

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
</div>

---

<div style="text-align: center; padding: 40px 0; color: #4a5568;">
  <strong>TaskRunna Framework</strong> - Making async batch processing simple and powerful! 🚀
</div>

 