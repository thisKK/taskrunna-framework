package com.taskrunna.batch.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.simple.SimpleMeterRegistry

/**
 * Utility for configuring Prometheus metrics with TaskRunna.
 * Provides convenient factory methods for setting up metrics collection.
 */
object PrometheusConfig {

    /**
     * Create a BatchMetrics instance with Prometheus registry if available,
     * falling back to a simple meter registry for testing/development.
     */
    @JvmStatic
    fun createBatchMetrics(metricsPrefix: String = "taskrunna"): BatchMetrics {
        return try {
            // Try to create Prometheus registry if available on classpath
            val prometheusClass = Class.forName("io.micrometer.prometheus.PrometheusMeterRegistry")
            val registry = prometheusClass.getDeclaredConstructor().newInstance() as MeterRegistry
            MicrometerBatchMetrics(registry, metricsPrefix)
        } catch (e: ClassNotFoundException) {
            // Fallback to simple registry if Prometheus not available
            MicrometerBatchMetrics(SimpleMeterRegistry(), metricsPrefix)
        } catch (e: Exception) {
            // If anything fails, use no-op metrics
            NoOpBatchMetrics.INSTANCE
        }
    }

    /**
     * Create a BatchMetrics instance with a provided MeterRegistry.
     * Use this when you want full control over the registry configuration.
     */
    @JvmStatic
    fun createBatchMetrics(meterRegistry: MeterRegistry, metricsPrefix: String = "taskrunna"): BatchMetrics {
        return MicrometerBatchMetrics(meterRegistry, metricsPrefix)
    }

    /**
     * Create a no-op BatchMetrics instance that doesn't collect any metrics.
     * Useful for testing or when metrics are not needed.
     */
    @JvmStatic
    fun createNoOpBatchMetrics(): BatchMetrics {
        return NoOpBatchMetrics.INSTANCE
    }
}

/**
 * Extension function to make Prometheus registry creation more convenient.
 * Only works if Prometheus dependency is on the classpath.
 */
fun createPrometheusRegistry(): MeterRegistry {
    return try {
        val prometheusClass = Class.forName("io.micrometer.prometheus.PrometheusMeterRegistry")
        prometheusClass.getDeclaredConstructor().newInstance() as MeterRegistry
    } catch (e: Exception) {
        throw IllegalStateException(
            "Prometheus registry not available. Add 'io.micrometer:micrometer-registry-prometheus' dependency.",
            e,
        )
    }
}
