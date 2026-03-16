plugins {
    `java-gradle-plugin`
    `maven-publish`
}

group = "no.domstolene"
version = providers.gradleProperty("releaseVersion").orElse("dev-SNAPSHOT").get()

val githubPackagesUser = providers.gradleProperty("GITHUB_USER")
    .orElse(providers.gradleProperty("githubUser"))
    .orElse(providers.environmentVariable("GITHUB_ACTOR"))
    .orElse(providers.environmentVariable("GITHUB_USER"))

val githubPackagesToken = providers.gradleProperty("GITHUB_TOKEN")
    .orElse(providers.gradleProperty("githubToken"))
    .orElse(providers.environmentVariable("GITHUB_TOKEN"))

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

val functionalTest by sourceSets.creating {
    compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
    runtimeClasspath += output + compileClasspath
}

configurations[functionalTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[functionalTest.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    add("functionalTestImplementation", gradleTestKit())
}

gradlePlugin {
    plugins {
        create("onePasswordGradleProperties") {
            id = "no.domstolene.gradle.properties.1password"
            implementationClass = "no.domstolene.gradle.properties.onepassword.OnePasswordGradlePropertiesPlugin"
            displayName = "1Password-backed Gradle properties"
            description = "Resolves project properties prefixed with op:// through the 1Password CLI."
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

val functionalTestTask = tasks.register<Test>("functionalTest") {
    description = "Runs functional tests with Gradle TestKit."
    group = LifecycleBasePlugin.VERIFICATION_GROUP
    testClassesDirs = functionalTest.output.classesDirs
    classpath = functionalTest.runtimeClasspath
    useJUnitPlatform()
    shouldRunAfter(tasks.test)
}

tasks.check {
    dependsOn(functionalTestTask)
}

configure<org.gradle.api.publish.PublishingExtension> {
    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/domstolene/gradle-properties-1password-plugin")
            credentials {
                username = githubPackagesUser.orNull
                password = githubPackagesToken.orNull
            }
        }
    }
}

