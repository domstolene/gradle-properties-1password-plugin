package no.domstolene.gradle.properties.onepassword;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.UnexpectedBuildFailure;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

class OnePasswordGradlePropertiesPluginFunctionalTest {

    @TempDir
    Path projectDir;

    @Test
    void resolvesProjectPropertyFromOnePasswordReference() throws IOException {
        assumePosix();
        Path opMock = createOpMock("echo \"functional-secret\"");
        writeProjectFiles(opMock, "TOKEN=op://vault/item/field");

        BuildResult result = runBuild("printToken");

        assertTrue(result.getOutput().contains("TOKEN=functional-secret"));
    }

    @Test
    void surfacesInvalidReferenceWithPropertyContext() throws IOException {
        assumePosix();
        Path opMock = createOpMock("echo \"ignored\"");
        writeProjectFiles(opMock, "TOKEN=op://");

        UnexpectedBuildFailure failure = assertThrows(
                UnexpectedBuildFailure.class,
                () -> runBuild("printToken")
        );

        assertTrue(failure.getMessage().contains("Property 'TOKEN'"));
        assertTrue(failure.getMessage().contains("invalid 1Password reference"));
    }

    private void writeProjectFiles(Path opMock, String tokenProperty) throws IOException {
        Files.writeString(projectDir.resolve("settings.gradle.kts"), "rootProject.name = \"functional-test\"\n");
        Files.writeString(
                projectDir.resolve("gradle.properties"),
                tokenProperty + "\n" +
                        "onePassword.op.command=" + opMock + "\n"
        );
        Files.writeString(
                projectDir.resolve("build.gradle.kts"),
                "plugins {\n" +
                        "    id(\"no.domstolene.gradle.properties.1password\")\n" +
                        "}\n" +
                        "\n" +
                        "tasks.register(\"printToken\") {\n" +
                        "    doLast {\n" +
                        "        println(\"TOKEN=\" + project.property(\"TOKEN\"))\n" +
                        "    }\n" +
                        "}\n"
        );
    }

    private BuildResult runBuild(String taskName) {
        return GradleRunner.create()
                .withProjectDir(projectDir.toFile())
                .withPluginClasspath()
                .withArguments(taskName, "--stacktrace")
                .build();
    }

    private Path createOpMock(String behavior) throws IOException {
        Path script = projectDir.resolve("op-mock.sh");
        Files.writeString(
                script,
                "#!/usr/bin/env bash\n" +
                        "set -euo pipefail\n" +
                        behavior + "\n"
        );
        Files.setPosixFilePermissions(
                script,
                Set.of(
                        PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE,
                        PosixFilePermission.OWNER_EXECUTE
                )
        );
        return script;
    }

    private void assumePosix() {
        assumeTrue(
                !System.getProperty("os.name").toLowerCase().contains("win"),
                "POSIX scripts are required for this test."
        );
    }
}
