plugins {
    `java-gradle-plugin`
}

group = "no.domstolene"
version = "0.1.0"

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
