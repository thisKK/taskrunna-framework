# Contributing to TaskRunna Framework

## Development Environment Setup

### Option 1: Using Devbox (Recommended)

[Devbox](https://www.jetpack.io/devbox) provides a reproducible development environment:

```bash
# Install devbox (if not already installed)
curl -fsSL https://get.jetpack.io/devbox | bash

# Enter the development environment
devbox shell

# Setup project (first time only)
devbox run setup

# Build the project
devbox run build

# Run examples
devbox run example
```

### Option 2: Manual Setup

Requirements:
- Java 17+
- Gradle 8.4+
- Kotlin 1.9.20+

```bash
./gradlew build
```

## Module Structure

- `taskrunna-core`: Core interfaces and base classes
- `taskrunna-batch`: Batch processing implementations  
- `taskrunna-examples`: Usage examples and demos

## Available Scripts (via devbox)

- `devbox run build` - Build the project
- `devbox run test` - Run tests
- `devbox run example` - Run example application
- `devbox run clean` - Clean build artifacts
- `devbox run setup` - Initial project setup

## Development Guidelines

- Follow Kotlin coding conventions
- Add comprehensive KDoc comments
- Include unit tests for new functionality
- Update examples when adding new features 