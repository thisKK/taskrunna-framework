// TaskRunna - Complete framework

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.google.guava:guava:33.4.8-jre")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.3")
    implementation("ch.qos.logback:logback-classic:1.5.15")

    // Metrics support
    implementation("io.micrometer:micrometer-core:1.14.2")
    compileOnly("io.micrometer:micrometer-registry-prometheus:1.14.2")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    testImplementation("io.mockk:mockk:1.13.14")
    testImplementation("org.hamcrest:hamcrest:3.0")
    testImplementation("org.apache.kafka:kafka-clients:3.9.0")
    testImplementation("io.micrometer:micrometer-registry-prometheus:1.14.2")
}
