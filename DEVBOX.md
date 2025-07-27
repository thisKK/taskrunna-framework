# Devbox Quick Reference

## Getting Started

```bash
# 1. Install devbox (one-time setup)
curl -fsSL https://get.jetpack.io/devbox | bash

# 2. Enter development environment
devbox shell

# 3. Setup project (first time only)
devbox run setup
```

## Common Commands

| Command | Description |
|---------|-------------|
| `devbox shell` | Enter development environment |
| `devbox run build` | Build the entire project |
| `devbox run test` | Run all tests |
| `devbox run example` | Run the example application |
| `devbox run clean` | Clean build artifacts |
| `devbox run setup` | Initial project setup |

## Environment Details

- **Java**: OpenJDK 17
- **Gradle**: 8.4  
- **Kotlin**: 1.9.20

## Benefits

✅ **Consistent Environment** - Everyone uses the same tool versions  
✅ **Easy Setup** - No manual installation of Java, Gradle, Kotlin  
✅ **Isolated** - Doesn't conflict with your system tools  
✅ **Fast** - Uses Nix for efficient package management  

## Troubleshooting

### Environment not loading?
```bash
devbox shell --refresh
```

### Need to update packages?
```bash
devbox update
```

### Want to see what's installed?
```bash
devbox info
``` 