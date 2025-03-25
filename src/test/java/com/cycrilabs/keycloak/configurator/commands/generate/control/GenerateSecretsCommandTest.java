package com.cycrilabs.keycloak.configurator.commands.generate.control;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@Order(2)
@QuarkusMainTest
class GenerateSecretsCommandTest {
    @Test
    @Launch(value = "rotate-secrets", exitCode = 2)
    public void shouldError_MissingParameters(final LaunchResult result) {
        Assertions.assertTrue(result.getErrorOutput().contains("Missing required options"));
    }

    @Test
    @Launch(value = { "rotate-secrets", "-s", "http://localhost:8080", "-u", "keycloak", "-p",
            "root", "-r", "realm-a" })
    public void shouldRotateSecrete(final LaunchResult result) {
        Assertions.assertTrue(result.getOutput().contains("Generated secrets for 1 clients"));
    }
}
