# gradle-properties-1password-plugin

This plugin resolves Gradle project properties prefixed with `op://` by calling the 1Password CLI (`op read <reference>`).

## Usage

`~/.gradle/gradle.properties`:

```properties
GITHUB_TOKEN=op://Personal/Github Personal Access Token/token
```

`build.gradle.kts`:

```kotlin
plugins {
    id("no.domstolene.gradle.properties.1password") version "0.1.0"
}

val githubToken: String by project

repositories {
    maven {
        url = uri("https://maven.pkg.github.com/domstolene/repo")
        credentials {
            username = "token"
            password = githubToken
        }
    }
}
```

## Behavior

- String property values starting with `op://` are resolved through `op read`.
- String property values not starting with `op://` are left unchanged.
- The resolved value is trimmed before being set as the project property.
- Secret values are never included in plugin error messages.

## Configuration

Optional properties in `gradle.properties`:

```properties
# Default: op
onePassword.op.command=/usr/local/bin/op

# Default: 10000 (10 seconds)
onePassword.op.timeoutMillis=10000
```

## Troubleshooting

- `Unable to execute 1Password CLI command ...`
  Ensure `op` is installed and accessible on `PATH`, or configure `onePassword.op.command`.

- `1Password CLI exited with code ...`
  The CLI command failed (for example, not signed in, missing vault/item/field, or access denied).

- `1Password CLI timed out ...`
  Increase `onePassword.op.timeoutMillis` or investigate local CLI/network conditions.

- `invalid 1Password reference`
  The property value starts with `op://` but does not contain a usable reference.
