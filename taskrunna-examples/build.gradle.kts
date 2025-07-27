// Examples module

plugins {
    application
}

application {
    mainClass.set("com.taskrunna.examples.PrometheusMetricsExampleKt")
}

dependencies {
    implementation(project(":taskrunna-core"))
    implementation(project(":taskrunna-batch"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.google.guava:guava:32.1.3-jre")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    // Metrics with Prometheus support
    implementation("io.micrometer:micrometer-registry-prometheus:1.12.0")

    // Ktor for HTTP server (metrics endpoint)
    implementation("io.ktor:ktor-server-core:2.3.6")
    implementation("io.ktor:ktor-server-netty:2.3.6")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
}
