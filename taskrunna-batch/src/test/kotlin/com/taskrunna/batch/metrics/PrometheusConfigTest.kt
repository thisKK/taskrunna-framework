package com.taskrunna.batch.metrics

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PrometheusConfigTest {

    @Test
    fun `should create batch metrics with custom prefix`() {
        val metrics = PrometheusConfig.createBatchMetrics("custom_prefix")

        assertNotNull(metrics)
        // Should fallback to MicrometerBatchMetrics with SimpleMeterRegistry
        // when Prometheus is not available
        assertTrue(metrics is MicrometerBatchMetrics || metrics is NoOpBatchMetrics)
    }

    @Test
    fun `should create batch metrics with provided meter registry`() {
        val registry = SimpleMeterRegistry()
        val metrics = PrometheusConfig.createBatchMetrics(registry, "test_prefix")

        assertNotNull(metrics)
        assertTrue(metrics is MicrometerBatchMetrics)
    }

    @Test
    fun `should create no-op batch metrics`() {
        val metrics = PrometheusConfig.createNoOpBatchMetrics()

        assertNotNull(metrics)
        assertTrue(metrics is NoOpBatchMetrics)
    }

    @Test
    fun `should create batch metrics with default prefix`() {
        val metrics = PrometheusConfig.createBatchMetrics()

        assertNotNull(metrics)
        // Should work without throwing exceptions
    }
}
