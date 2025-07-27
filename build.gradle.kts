plugins {
    kotlin("jvm") version "2.2.0" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1" apply false
    id("org.jetbrains.dokka") version "2.0.0" apply false
}

allprojects {
    group = "com.taskrunna"
    version = "1.1.1"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    
    // Only apply publishing to library modules (not examples)
    if (name != "taskrunna-examples") {
        apply(plugin = "maven-publish")
        apply(plugin = "signing")
        apply(plugin = "org.jetbrains.dokka")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    extensions.configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(17)
    }

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("0.50.0")
        debug.set(false)
        verbose.set(true)
        android.set(false)
        outputToConsole.set(true)
        outputColorName.set("RED")
        ignoreFailures.set(false)
        enableExperimentalRules.set(false)
        
        filter {
            exclude("**/generated/**")
            include("**/kotlin/**")
        }
    }
    
    // Publishing configuration for library modules
    if (name != "taskrunna-examples") {
        configure<PublishingExtension> {
            publications {
                create<MavenPublication>("maven") {
                    from(components["java"])
                    
                    // Add sources jar
                    artifact(tasks.register("sourcesJar", Jar::class) {
                        from(project.the<SourceSetContainer>()["main"].allSource)
                        archiveClassifier.set("sources")
                    })
                    
                    // Add javadoc jar
                    artifact(tasks.register("javadocJar", Jar::class) {
                        from(tasks.named("dokkaHtml"))
                        archiveClassifier.set("javadoc")
                    })
                    
                    pom {
                        name.set("TaskRunna ${project.name.removePrefix("taskrunna-").replaceFirstChar { it.uppercase() }}")
                        description.set("TaskRunna - Lightweight, modular job orchestration framework for asynchronous task execution")
                        url.set("https://github.com/thisKK/taskrunna-framework")
                        
                        licenses {
                            license {
                                name.set("MIT License")
                                url.set("https://opensource.org/licenses/MIT")
                            }
                        }
                        
                        developers {
                            developer {
                                id.set("thisKK")
                                name.set("thisKK")
                                email.set("piampoon.kay@gmail.com")
                            }
                        }
                        
                        scm {
                            connection.set("scm:git:git://github.com/thisKK/taskrunna-framework.git")
                            developerConnection.set("scm:git:ssh://github.com/thisKK/taskrunna-framework.git")
                            url.set("https://github.com/thisKK/taskrunna-framework")
                        }
                    }
                }
            }
            
            repositories {
                // GitHub Packages
                maven {
                    name = "GitHubPackages"
                    url = uri("https://maven.pkg.github.com/thisKK/taskrunna-framework")
                    credentials {
                        username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
                        password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
                    }
                }
                
                // Maven Central (Sonatype)
                maven {
                    name = "OSSRH"
                    url = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                    credentials {
                        username = project.findProperty("ossrh.username") as String? ?: System.getenv("OSSRH_USERNAME")
                        password = project.findProperty("ossrh.password") as String? ?: System.getenv("OSSRH_PASSWORD")
                    }
                }
            }
        }
        
        // Signing configuration (required for Maven Central)
        configure<SigningExtension> {
            val signingKey = project.findProperty("signing.key") as String? ?: System.getenv("SIGNING_KEY")
            val signingPassword = project.findProperty("signing.password") as String? ?: System.getenv("SIGNING_PASSWORD")
            
            if (signingKey != null && signingPassword != null) {
                useInMemoryPgpKeys(signingKey, signingPassword)
                sign(extensions.getByType<PublishingExtension>().publications)
            }
        }
    }
} 