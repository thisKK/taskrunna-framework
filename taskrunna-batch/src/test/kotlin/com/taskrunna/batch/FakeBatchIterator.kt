package com.taskrunna.batch

import com.taskrunna.core.BaseBatchIterator

class FakeBatchIterator : BaseBatchIterator<String>() {
    private val allRecords = listOf("A", "B", "C", "D", "E")
    private var index = 0

    override fun loadNextBatch(afterCursor: String, batchSize: Int): Collection<String> {
        if (index >= allRecords.size) return emptyList()
        val nextBatch = allRecords.subList(index, (index + 2).coerceAtMost(allRecords.size))
        index += nextBatch.size
        return nextBatch
    }

    override fun extractCursorFrom(item: String): String = item
} 