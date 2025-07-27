# Changelog

All notable changes to TaskRunna Framework will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.1.0] - 2024-07-27

### 🎯 Major Simplification - Single Package Release

**This is a significant improvement that makes TaskRunna much easier to use!**

### Added
- **Single Package Architecture**: Everything now in `com.taskrunna:taskrunna:1.1.0`
- **Comprehensive Prometheus Metrics**: Built-in observability with 8+ metrics
- **Production-Ready Examples**: Working order processing demo with HTTP server
- **Publishing Infrastructure**: Available on GitHub Packages
- **Complete Documentation**: PUBLISHING.md, METRICS.md, and enhanced README

### Changed
- **BREAKING**: Package imports changed from `com.taskrunna.core.*` to `com.taskrunna.batch.*`
- **BREAKING**: Single dependency replaces `taskrunna-core` + `taskrunna-batch`
- **Simplified Installation**: One import instead of two
- **Enhanced README**: Clearer examples and better value proposition
- **Project Structure**: Consolidated modules for better maintainability

### Technical Details
- Moved `BaseBatchIterator` from `taskrunna-core` to `taskrunna` package
- Removed `taskrunna-core` module entirely
- Updated all imports and examples
- Added ktlint for consistent code style
- Integrated Micrometer with Prometheus support

### Migration Guide

**From v1.0.0 to v1.1.0:**

```kotlin
// Before (v1.0.0)
dependencies {
    implementation("com.taskrunna:taskrunna-core:1.0.0")
    implementation("com.taskrunna:taskrunna-batch:1.0.0")
}
import com.taskrunna.core.BaseBatchIterator

// After (v1.1.0) 
dependencies {
    implementation("com.taskrunna:taskrunna:1.1.0")
}
import com.taskrunna.batch.BaseBatchIterator
```

## [1.0.0] - 2024-07-27 (Legacy)

### Added
- Initial release with multi-module structure
- Basic batch processing capabilities
- Async job execution with ListenableFuture
- Core abstractions and utilities

### Modules (Legacy)
- `taskrunna-core`: Core interfaces and utilities
- `taskrunna-batch`: Batch processing implementation

**Note**: v1.0.0 is available but v1.1.0 is strongly recommended for new projects. 