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

## Development

### Building

```bash
./gradlew build
```

Run unit tests only:

```bash
./gradlew test
```

Run functional tests only:

```bash
./gradlew functionalTest
```

### Using the plugin locally in another project

#### Option 1: Composite build (recommended, no publishing required)

Use a [composite build](https://docs.gradle.org/current/userguide/composite_builds.html) to include the plugin directly from source.

In the consuming project's `settings.gradle.kts`:

```kotlin
pluginManagement {
    includeBuild("/path/to/gradle-properties-1password-plugin")
}
```

Then apply the plugin as normal — no version number is needed:

```kotlin
plugins {
    id("no.domstolene.gradle.properties.1password")
}
```

#### Option 2: Publish to local Maven repository

1. Publish to your local Maven repository (`~/.m2`):

   ```bash
   ./gradlew publishToMavenLocal
   ```

2. In the consuming project's `settings.gradle.kts`, add `mavenLocal()` to the plugin repositories:

   ```kotlin
   pluginManagement {
       repositories {
           mavenLocal()
           gradlePluginPortal()
       }
   }
   ```

3. Apply the plugin with its version:

   ```kotlin
   plugins {
       id("no.domstolene.gradle.properties.1password") version "0.1.0"
   }
   ```

### Publishing to GitHub Packages

The plugin is published to the [GitHub Maven Package Registry](https://docs.github.com/en/packages/working-with-a-github-packages-registry/working-with-the-apache-maven-registry).

#### Authenticate

Publishing requires a [personal access token](https://github.com/settings/tokens) (classic) with the `write:packages` scope.
Supply the credentials either via `~/.gradle/gradle.properties`:

```properties
GITHUB_USER=YOUR_GITHUB_USERNAME
GITHUB_TOKEN=YOUR_GITHUB_TOKEN
```

or via environment variables (e.g. in CI):

```bash
export GITHUB_ACTOR=YOUR_GITHUB_USERNAME
export GITHUB_TOKEN=YOUR_GITHUB_TOKEN
```

#### Publish

```bash
./gradlew publishAllPublicationsToGitHubPackagesRepository
```

### Automated release publishing (GitHub Releases)

This repository includes a release workflow (`.github/workflows/release-publish.yml`) that runs when a GitHub Release is published.

- The plugin version is read from the release tag (for example `v1.2.3` or `1.2.3`).
- The workflow removes an optional `v` prefix and publishes with `-PreleaseVersion=<resolved-version>`.
- The workflow uses the repository `GITHUB_TOKEN` (`packages:write`) to publish to GitHub Packages.

For local development builds, the plugin version defaults to `dev-SNAPSHOT` unless `releaseVersion` is provided.

#### Consume

To use the published plugin from GitHub Packages in another project, add the repository to `settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        maven {
            url = uri("https://maven.pkg.github.com/domstolene/gradle-properties-1password-plugin")
            credentials {
                username = providers.gradleProperty("GITHUB_USER").orNull ?: System.getenv("GITHUB_ACTOR")
                password = providers.gradleProperty("GITHUB_TOKEN").orNull ?: System.getenv("GITHUB_TOKEN")
            }
        }
        gradlePluginPortal()
    }
}
```

Then apply the plugin:

```kotlin
plugins {
    id("no.domstolene.gradle.properties.1password") version "0.1.0"
}
```
