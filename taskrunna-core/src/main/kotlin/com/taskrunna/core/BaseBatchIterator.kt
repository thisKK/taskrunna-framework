package com.taskrunna.core

abstract class BaseBatchIterator<T>(
    private val batchSize: Int = 10_000,
) : Iterator<Collection<T>> {

    protected var records: Collection<T> = emptyList()
    private var afterCursor: String = ""
    private var batchNumber = 0
    private var batchStartTime: Long = 0

    abstract fun loadNextBatch(afterCursor: String, batchSize: Int): Collection<T>
    abstract fun extractCursorFrom(item: T): String

    override fun hasNext(): Boolean {
        batchStartTime = System.currentTimeMillis()
        records = loadNextBatch(afterCursor, batchSize)
        if (records.isNotEmpty()) {
            afterCursor = extractCursorFrom(records.last())
            batchNumber++
        }
        return records.isNotEmpty()
    }

    override fun next(): Collection<T> = records

    fun reportBatch(logPrefix: String = "Batch") {
        val durationSec = (System.currentTimeMillis() - batchStartTime) / 1000.0
        println("$logPrefix #$batchNumber - ${records.size} records in $durationSec sec.")
    }
} 