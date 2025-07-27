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
- `devbox run lint` - Check code style with ktlint
- `devbox run format` - Auto-format code with ktlint
- `devbox run check` - Run linting + tests (full quality check)

## Development Guidelines

- **Code Style**: Follow Kotlin coding conventions enforced by ktlint
- **Formatting**: Run `devbox run format` to auto-format your code
- **Quality**: Run `devbox run check` before committing to ensure code quality
- **Documentation**: Add comprehensive KDoc comments for public APIs
- **Testing**: Include unit tests for new functionality
- **Examples**: Update examples when adding new features

### Code Quality Workflow

1. Write your code
2. Run `devbox run format` to format it
3. Run `devbox run check` to verify style and tests
4. Commit your changes 