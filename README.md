# gradle-properties-1password-plugin
If a property is a path for an 1Password item, resolve through 1Password CLI.

## Example

~/.gradle/gradle.properties:
```properties
GITHUB_TOKEN=op://Personal/Github Personal Access Token/token
```

build.gradle.kts:

```kotlin
plugins {
    id("no.domstolene.gradle.properties.1password") version "0.1.0"
}

val GITHUB_TOKEN: String by project

repositories {
    maven {
        url = uri("https://maven.pkg.github.com/domstolene/repo")
        credentials {
            username = token
            password = GITHUB_TOKEN
        }
    }
}
```

## How it works
1. Whenever a property is resolved, the plugin checks if the value starts with `op://`. If it does, it will try to resolve the value through the 1Password CLI. If the value is successfully resolved, it will be returned as the property value. If not, an error will be thrown.