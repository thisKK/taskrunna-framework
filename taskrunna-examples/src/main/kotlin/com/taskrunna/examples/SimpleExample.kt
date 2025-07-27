package com.taskrunna.examples

import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.taskrunna.batch.BatchJobProcessor
import com.taskrunna.core.BaseBatchIterator
import io.github.oshai.kotlinlogging.KotlinLogging

private val logger = KotlinLogging.logger {}

// Example data class
data class Task(val id: String, val data: String)

// Example iterator implementation
class SimpleTaskIterator(
    private val tasks: List<Task>,
    batchSize: Int = 10
) : BaseBatchIterator<Task>(batchSize) {
    
    private var currentIndex = 0

    override fun loadNextBatch(afterCursor: String, batchSize: Int): Collection<Task> {
        if (currentIndex >= tasks.size) return emptyList()
        
        val endIndex = minOf(currentIndex + batchSize, tasks.size)
        val batch = tasks.subList(currentIndex, endIndex)
        currentIndex = endIndex
        
        return batch
    }

    override fun extractCursorFrom(item: Task): String = item.id
}

// Example usage
fun main() {
    // Sample data
    val tasks = (1..25).map { Task(id = "task-$it", data = "data-$it") }
    
    // Create processor
    val processor = BatchJobProcessor(
        iterator = SimpleTaskIterator(tasks, batchSize = 5),
        submitJob = { task ->
            // Simulate async work (e.g., HTTP call, Kafka send)
            processTaskAsync(task)
        },
        onSuccess = { task, result ->
            logger.info { "Successfully processed task ${task.id}: $result" }
        },
        onFailure = { task, error ->
            logger.error(error) { "Failed to process task ${task.id}" }
        },
        logger = logger
    )
    
    // Run the batch job
    processor.run()
}

// Simulate async task processing
private fun processTaskAsync(task: Task): ListenableFuture<String> {
    return Futures.immediateFuture("processed-${task.id}")
} 