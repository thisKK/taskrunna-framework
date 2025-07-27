// Examples module

plugins {
    application
}

application {
    mainClass.set("com.taskrunna.examples.SimpleExampleKt")
}

dependencies {
    implementation(project(":taskrunna-core"))
    implementation(project(":taskrunna-batch"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.google.guava:guava:32.1.3-jre")
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.0")
    implementation("ch.qos.logback:logback-classic:1.4.11")
} 