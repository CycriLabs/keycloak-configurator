package com.cycrilabs.keycloak.configurator.commands.configure.control;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@Order(1)
@QuarkusMainTest
class ConfigureCommandTest {
    @Test
    @Launch(value = "configure", exitCode = 2)
    public void shouldError_MissingParameters(final LaunchResult result) {
        Assertions.assertTrue(result.getErrorOutput().contains("Missing required options"));
    }

    @Test
    @Launch(value = { "configure", "-s", "http://localhost:8080", "-u", "keycloak", "-p", "root",
            "-c", "./src/test/resources/configuration" })
    public void shouldRotateSecrete(final LaunchResult result) {
        final String output = result.getOutput();
        Assertions.assertTrue(output.contains("Executing importer 'RealmImporter'"));
        Assertions.assertTrue(output.contains("Executing importer 'ComponentImporter'"));
    }
}
