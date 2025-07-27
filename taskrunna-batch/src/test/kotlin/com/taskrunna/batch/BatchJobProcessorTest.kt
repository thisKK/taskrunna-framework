package com.taskrunna.batch

import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import com.taskrunna.core.BaseBatchIterator
import io.github.oshai.kotlinlogging.KLogger
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors

class BatchJobProcessorTest {
    @Test
    fun `should process all batches across multiple pages`() {
        // Fake iterator that returns 2 items per batch: ["A", "B"], ["C", "D"], ["E"], then empty
        class PagedBatchIterator : BaseBatchIterator<String>(batchSize = 2) {
            private val data = listOf("A", "B", "C", "D", "E")
            private var currentIndex = 0

            override fun loadNextBatch(afterCursor: String, batchSize: Int): Collection<String> {
                if (currentIndex >= data.size) return emptyList()
                
                val endIndex = minOf(currentIndex + batchSize, data.size)
                val batch = data.subList(currentIndex, endIndex)
                currentIndex = endIndex
                
                return batch
            }

            override fun extractCursorFrom(item: String): String = item
        }

        val processed = mutableListOf<String>()
        val processor = BatchJobProcessor(
            iterator = PagedBatchIterator(),
            submitJob = { item -> SettableFuture.create<String>().apply { set("ok-$item") } },
            onSuccess = { item, _ -> processed.add(item) },
            onFailure = { _, _ -> fail("No failures expected") },
            logger = mockk(relaxed = true),
        )

        processor.run()

        assertEquals(listOf("A", "B", "C", "D", "E"), processed)
    }

    @Test
    fun `should process all items and call onSuccess`() {
        val iterator = FakeBatchIterator()
        val logger = mockk<KLogger>(relaxed = true)
        val submitJob = mockk<(String) -> SettableFuture<String>>()
        val onSuccess = mockk<(String, String?) -> Unit>(relaxed = true)
        val onFailure = mockk<(String, Throwable) -> Unit>(relaxed = true)

        // Prepare futures for each input
        every { submitJob(any()) } answers {
            SettableFuture.create<String>().also { it.set("ok-${firstArg<String>()}") }
        }

        val processor = BatchJobProcessor(
            iterator = iterator,
            submitJob = submitJob,
            onSuccess = onSuccess,
            onFailure = onFailure,
            logger = logger,
        )

        processor.run()

        verify(exactly = 5) { submitJob(any()) }
        verify(exactly = 5) { onSuccess(any(), match { it.startsWith("ok-") }) }
        verify(exactly = 0) { onFailure(any(), any()) }
    }

    @Test
    fun `should call onFailure when submitJob fails`() {
        val iterator = FakeBatchIterator()
        val logger = mockk<KLogger>(relaxed = true)
        val submitJob = mockk<(String) -> SettableFuture<String>>()
        val onSuccess = mockk<(String, String?) -> Unit>(relaxed = true)
        val onFailure = mockk<(String, Throwable) -> Unit>(relaxed = true)

        every { submitJob(match { it == "B" }) } answers {
            SettableFuture.create<String>().apply {
                setException(RuntimeException("failed B"))
            }
        }
        every { submitJob(not("B")) } answers {
            SettableFuture.create<String>().also { it.set("ok-${firstArg<String>()}") }
        }

        val processor = BatchJobProcessor(
            iterator = iterator,
            submitJob = submitJob,
            onSuccess = onSuccess,
            onFailure = onFailure,
            logger = logger,
        )

        processor.run()

        verify(exactly = 5) { submitJob(any()) }
        verify(exactly = 4) { onSuccess(any(), any()) }
        verify(exactly = 1) { onFailure(eq("B"), any()) }
    }

    @Test
    fun `should exit gracefully when no records to process`() {
        val iterator = object : BaseBatchIterator<String>() {
            var called = false
            override fun loadNextBatch(afterCursor: String, batchSize: Int): Collection<String> {
                called = true
                return emptyList()
            }

            override fun extractCursorFrom(item: String): String = item
        }

        val submitJob = mockk<(String) -> ListenableFuture<String>>()
        val processor = BatchJobProcessor(
            iterator = iterator,
            submitJob = submitJob,
            onSuccess = { _, _ -> },
            onFailure = { _, _ -> },
            logger = mockk(relaxed = true),
        )

        processor.run()

        // Just ensure no jobs were submitted
        verify(exactly = 0) { submitJob(any()) }
    }
} 