// TaskRunna - Complete framework

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.google.guava:guava:32.1.3-jre")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("ch.qos.logback:logback-classic:1.4.11")

    // Metrics support
    implementation("io.micrometer:micrometer-core:1.12.0")
    compileOnly("io.micrometer:micrometer-registry-prometheus:1.12.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.hamcrest:hamcrest:2.2")
    testImplementation("org.apache.kafka:kafka-clients:3.6.0")
    testImplementation("io.micrometer:micrometer-registry-prometheus:1.12.0")
}
