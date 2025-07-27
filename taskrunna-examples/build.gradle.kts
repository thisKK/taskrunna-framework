// Examples module

plugins {
    application
}

application {
    mainClass.set("com.taskrunna.examples.PrometheusMetricsExampleKt")
}

dependencies {
    implementation(project(":taskrunna"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.google.guava:guava:33.4.8-jre")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("ch.qos.logback:logback-classic:1.5.15")

    // Metrics with Prometheus support
    implementation("io.micrometer:micrometer-registry-prometheus:1.14.2")

    // Ktor for HTTP server (metrics endpoint)
    implementation("io.ktor:ktor-server-core:3.1.0")
    implementation("io.ktor:ktor-server-netty:3.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")
}
