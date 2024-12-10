package com.cycrilabs.keycloak.configurator.commands.configure.control;

import jakarta.inject.Inject;

import com.cycrilabs.keycloak.configurator.shared.control.KeycloakOptions;

import picocli.CommandLine;

@CommandLine.Command(name = "configure", mixinStandardHelpOptions = true)
public class ConfigureCommand implements Runnable {
    @CommandLine.Mixin
    KeycloakOptions keycloakOptions;
    @CommandLine.Option(required = true, names = { "-c", "--config" },
            description = "Directory containing the keycloak configuration files.")
    String configDirectory = "";
    @CommandLine.Option(names = { "-t", "--entity-type" },
            description = "Entity type to configure. If not provided, all entities are configured.")
    String entityType;
    @CommandLine.Option(names = { "--exit-on-error"},
            description = "Exit the application if an error occurs during configuration.")
    boolean exitOnError;

    @Inject
    ConfigurationFileStore configurationFileStore;
    @Inject
    ImportRunner importRunner;

    @Override
    public void run() {
        configurationFileStore.init();
        importRunner.run();
    }
}
