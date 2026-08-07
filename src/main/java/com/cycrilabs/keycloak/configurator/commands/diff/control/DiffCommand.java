package com.cycrilabs.keycloak.configurator.commands.diff.control;

import static com.cycrilabs.keycloak.configurator.shared.control.MeasuredMethodExecutor.measureExecutionTime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.inject.Inject;

import com.cycrilabs.keycloak.configurator.commands.configure.control.ConfigurationFileStore;
import com.cycrilabs.keycloak.configurator.commands.diff.entity.ServerUnreachableException;
import com.cycrilabs.keycloak.configurator.shared.control.EntityTypeConverter;
import com.cycrilabs.keycloak.configurator.shared.control.KeycloakOptions;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;
import picocli.CommandLine;

/**
 * Reports all differences between a Keycloak server and the local configuration without modifying
 * the server.
 */
@CommandLine.Command(name = "diff", mixinStandardHelpOptions = true)
public class DiffCommand implements Callable<Integer> {
    /**
     * Exit code returned when the server matches the local configuration.
     */
    private static final int EXIT_CODE_IN_SYNC = 0;
    /**
     * Exit code returned when differences were found, so that the command can be used as a check.
     */
    private static final int EXIT_CODE_DIFFERENCES_FOUND = 1;
    /**
     * Exit code returned when the server could not be compared at all. This is deliberately not
     * the exit code of a found difference, so that an unreachable server is not mistaken for a
     * configuration that was never applied.
     */
    private static final int EXIT_CODE_SERVER_UNREACHABLE = 3;

    @CommandLine.Mixin
    KeycloakOptions keycloakOptions;
    @CommandLine.Option(required = true, names = { "-c", "--config" },
            description = "Directory containing the keycloak configuration files.")
    String configDirectory = "";
    @CommandLine.Option(names = { "-t", "--entity-type" },
            description = "Entity type to compare. If not provided, all entities are compared.",
            converter = EntityTypeConverter.class)
    EntityType entityType;
    @CommandLine.Option(names = { "--flat-files" },
            description = "Read configuration files from a flat file list instead of nested type directories.")
    boolean flatFiles;
    @CommandLine.Option(names = { "--include-built-ins" },
            description = "Also report Keycloak built-in and auto-generated entities that are not configured locally.")
    boolean includeBuiltIns;
    @CommandLine.Option(names = { "--ignore-extra" }, paramLabel = "<type>:<name>",
            description = "Entity that must not be reported when it only exists on the server, e.g. 'client:legacy-app'. Can be given multiple times.")
    List<String> ignoreExtra = new ArrayList<>();

    @Inject
    ConfigurationFileStore configurationFileStore;
    @Inject
    DiffRunner diffRunner;

    @Override
    public Integer call() {
        final AtomicInteger differences = new AtomicInteger();
        try {
            measureExecutionTime(() -> {
                configurationFileStore.init();
                differences.set(diffRunner.run());
            }, "DiffCommand");
        } catch (final ServerUnreachableException e) {
            Log.error(e.getMessage());
            return EXIT_CODE_SERVER_UNREACHABLE;
        }

        return differences.get() == 0
               ? EXIT_CODE_IN_SYNC
               : EXIT_CODE_DIFFERENCES_FOUND;
    }
}
