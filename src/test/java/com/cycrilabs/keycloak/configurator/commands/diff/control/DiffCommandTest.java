package com.cycrilabs.keycloak.configurator.commands.diff.control;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.main.Launch;
import io.quarkus.test.junit.main.LaunchResult;
import io.quarkus.test.junit.main.QuarkusMainTest;

@Order(3)
@QuarkusMainTest
class DiffCommandTest {
    @Test
    @Launch(value = "diff", exitCode = 2)
    public void shouldError_MissingParameters(final LaunchResult result) {
        Assertions.assertTrue(result.getErrorOutput().contains("Missing required options"));
    }

    @Test
    @Launch(value = { "diff", "-h" })
    public void shouldPrintHelp(final LaunchResult result) {
        final String output = result.getOutput();
        Assertions.assertTrue(output.contains("--flat-files"));
        Assertions.assertTrue(output.contains("--include-built-ins"));
        Assertions.assertTrue(output.contains("--ignore-extra"));
    }

    /**
     * An unreachable server must not be reported as a configuration that is missing on the
     * server, as those are entirely different problems.
     */
    @Test
    @Launch(value = { "diff", "-s", "http://localhost:1", "-u", "keycloak", "-p", "root", "-c",
            "./src/test/resources/configuration-missing" }, exitCode = 3)
    public void shouldFail_ServerIsUnreachable(final LaunchResult result) {
        final String output = result.getOutput();

        Assertions.assertTrue(output.contains("Could not authenticate against server"));
        Assertions.assertFalse(output.contains("MISSING on server"));
        Assertions.assertFalse(output.contains("differences across"));
    }

    /**
     * Compares a configuration whose realm does not exist on the server. This is independent of
     * the entities any other test creates, so the reported differences are deterministic.
     */
    @Test
    @Launch(value = { "diff", "-s", "http://localhost:8080", "-u", "keycloak", "-p", "root", "-c",
            "./src/test/resources/configuration-missing" }, exitCode = 1)
    public void shouldReportMissingEntities_RealmDoesNotExistOnServer(final LaunchResult result) {
        final String output = result.getOutput();

        Assertions.assertTrue(output.contains("The server is not modified."));
        Assertions.assertTrue(output.contains("Executing differ 'RealmDiffer'"));
        Assertions.assertTrue(output.contains("Executing differ 'ComponentDiffer'"));

        Assertions.assertTrue(output.contains("=== realm-unknown ==="));
        Assertions.assertTrue(output.contains("realm 'realm-unknown'"));
        Assertions.assertTrue(output.contains("MISSING on server"));
    }

    /**
     * The flat file layout must be read by the 'diff' command as well.
     */
    @Test
    @Launch(value = { "diff", "-s", "http://localhost:8080", "-u", "keycloak", "-p", "root", "-c",
            "./src/test/resources/configuration-missing-flat", "--flat-files" }, exitCode = 1)
    public void shouldReadFlatFileLayout(final LaunchResult result) {
        final String output = result.getOutput();

        Assertions.assertTrue(output.contains("Using 'FlatFileConfigurationFileLoader'"));
        Assertions.assertTrue(output.contains("realm 'realm-unknown'"));
        Assertions.assertTrue(output.contains("MISSING on server"));
    }

    /**
     * Comparing a single entity type must not report findings of any other type.
     */
    @Test
    @Launch(value = { "diff", "-s", "http://localhost:8080", "-u", "keycloak", "-p", "root", "-c",
            "./src/test/resources/configuration-missing", "-t", "realm" }, exitCode = 1)
    public void shouldOnlyCompareGivenEntityType(final LaunchResult result) {
        final String output = result.getOutput();

        Assertions.assertTrue(output.contains("Executing differ 'RealmDiffer'"));
        Assertions.assertFalse(output.contains("Executing differ 'ClientDiffer'"));
        Assertions.assertTrue(output.contains("realm 'realm-unknown'"));
        Assertions.assertFalse(output.contains("client 'unknown-client'"));
    }
}
