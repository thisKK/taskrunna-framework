# Publishing TaskRunna Framework 📦

This guide explains how to publish TaskRunna to various package repositories.

## Prerequisites

- Java 17+
- Gradle 8.0+
- Appropriate repository credentials

## Quick Start

### Test Local Publishing

```bash
# Build and publish to local Maven repository
./gradlew publishToMavenLocal

# Verify artifacts
ls ~/.m2/repository/com/taskrunna/
```

### Publish to GitHub Packages

1. **Create GitHub Personal Access Token:**
   - Go to GitHub Settings → Developer settings → Personal access tokens
   - Create token with `write:packages` permission

2. **Configure credentials** in `~/.gradle/gradle.properties`:
   ```properties
   gpr.user=your-github-username
   gpr.key=your_personal_access_token
   ```

3. **Update repository URLs** in `build.gradle.kts`:
   ```kotlin
   url = uri("https://maven.pkg.github.com/your-username/taskrunna-framework")
   ```

4. **Publish:**
   ```bash
   ./gradlew publishMavenPublicationToGitHubPackagesRepository
   ```

### Publish to Maven Central

1. **Create Sonatype Account:**
   - Sign up at [https://central.sonatype.org/](https://central.sonatype.org/)
   - Request namespace for your group ID

2. **Generate GPG Key:**
   ```bash
   # Generate key pair
   gpg --gen-key
   
   # Export private key (for signing)
   gpg --export-secret-keys YOUR_KEY_ID | base64 > private-key.txt
   
   # Upload public key to keyserver
   gpg --keyserver keyserver.ubuntu.com --send-keys YOUR_KEY_ID
   ```

3. **Configure credentials** in `~/.gradle/gradle.properties`:
   ```properties
   ossrh.username=your-sonatype-username
   ossrh.password=your-sonatype-password
   signing.key=your-base64-encoded-private-key
   signing.password=your-pgp-key-password
   ```

4. **Update repository URLs** in `build.gradle.kts`:
   ```kotlin
   url.set("https://github.com/your-username/taskrunna-framework")
   # Update all GitHub URLs to your actual repository
   ```

5. **Publish:**
   ```bash
   ./gradlew publishMavenPublicationToOSSRHRepository
   ```

## Publishing Configuration

### Current Setup

The project is configured to publish:
- **taskrunna-core** - Core interfaces and utilities
- **taskrunna-batch** - Batch processing implementation
- **taskrunna-examples** - Not published (examples only)

### Artifacts Included

Each published module contains:
- **Main JAR** - Compiled classes
- **Sources JAR** - Source code
- **Javadoc JAR** - Generated documentation

### Version Management

Update version in `build.gradle.kts`:
```kotlin
allprojects {
    group = "com.taskrunna"
    version = "1.0.0"  // Update this
}
```

## Repository Configuration

### Supported Repositories

1. **Local Maven** (`publishToMavenLocal`)
2. **GitHub Packages** (`publishMavenPublicationToGitHubPackagesRepository`)
3. **Maven Central** (`publishMavenPublicationToOSSRHRepository`)

### Adding Custom Repository

```kotlin
repositories {
    maven {
        name = "CustomRepo"
        url = uri("https://your-custom-repo.com/maven")
        credentials {
            username = project.findProperty("custom.username") as String?
            password = project.findProperty("custom.password") as String?
        }
    }
}
```

## Gradle Tasks

### Publishing Tasks

```bash
# List all publishing tasks
./gradlew tasks --group publishing

# Publish to specific repository
./gradlew publishMavenPublicationToGitHubPackagesRepository
./gradlew publishMavenPublicationToOSSRHRepository

# Publish to all configured repositories
./gradlew publish

# Publish to local repository
./gradlew publishToMavenLocal
```

### Build and Sign

```bash
# Build all artifacts
./gradlew build

# Sign artifacts (if signing configured)
./gradlew signMavenPublication

# Generate documentation
./gradlew dokkaHtml
```

## Verification

### Check Generated Artifacts

```bash
# Build artifacts
./gradlew build

# Check build outputs
ls -la */build/libs/

# Expected files:
# taskrunna-core-1.0.0.jar
# taskrunna-core-1.0.0-sources.jar
# taskrunna-core-1.0.0-javadoc.jar
# taskrunna-batch-1.0.0.jar
# taskrunna-batch-1.0.0-sources.jar
# taskrunna-batch-1.0.0-javadoc.jar
```

### Test Published Library

Create a test project and add dependency:

```kotlin
dependencies {
    implementation("com.taskrunna:taskrunna-batch:1.0.0")
}
```

## Troubleshooting

### Common Issues

1. **Signing Failed**
   ```
   Execution failed for task ':taskrunna-core:signMavenPublication'
   ```
   - Check GPG key configuration
   - Verify signing.key and signing.password are correct

2. **Authentication Failed**
   ```
   Could not publish to repository
   ```
   - Verify repository credentials
   - Check token/password permissions

3. **Missing Artifacts**
   ```
   Task 'javadocJar' not found
   ```
   - Run `./gradlew clean build` first
   - Check Dokka plugin is applied

### Environment Variables

For CI/CD, use environment variables:
```bash
export GITHUB_ACTOR=your-username
export GITHUB_TOKEN=your-token
export OSSRH_USERNAME=your-sonatype-username
export OSSRH_PASSWORD=your-sonatype-password
export SIGNING_KEY=your-base64-key
export SIGNING_PASSWORD=your-key-password
```

## Security Best Practices

1. **Never commit credentials** to version control
2. **Use environment variables** in CI/CD
3. **Store keys securely** (GitHub Secrets, etc.)
4. **Rotate tokens** regularly
5. **Use minimal permissions** (only `write:packages` for GitHub)

## Release Process

1. **Update version** in `build.gradle.kts`
2. **Update CHANGELOG.md**
3. **Create release branch**
4. **Run tests**: `./gradlew test`
5. **Build artifacts**: `./gradlew build`
6. **Publish**: `./gradlew publish`
7. **Create GitHub release**
8. **Update documentation**

## Support

For publishing issues:
- **GitHub Packages**: [GitHub Packages Documentation](https://docs.github.com/en/packages)
- **Maven Central**: [Central Repository Documentation](https://central.sonatype.org/publish/)
- **Project Issues**: Create issue in this repository 