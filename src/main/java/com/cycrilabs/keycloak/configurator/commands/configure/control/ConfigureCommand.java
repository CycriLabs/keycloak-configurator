package com.cycrilabs.keycloak.configurator.commands.configure.control;

import java.util.Comparator;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import com.cycrilabs.keycloak.configurator.commands.configure.boundary.AbstractImporter;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigureCommandConfiguration;
import com.cycrilabs.keycloak.configurator.shared.control.KeycloakOptions;

import io.quarkus.logging.Log;
import picocli.CommandLine;

@CommandLine.Command(name = "configure", mixinStandardHelpOptions = true)
public class ConfigureCommand implements Runnable {
    @CommandLine.Mixin
    KeycloakOptions keycloakOptions;
    @CommandLine.Option(required = true, names = { "-c", "--config" },
            description = "Directory containing the keycloak configuration files.")
    String configDirectory = "";

    @Inject
    EntityStore entityStore;
    @Inject
    ConfigureCommandConfiguration configuration;
    @Inject
    Instance<AbstractImporter> importers;

    @Override
    public void run() {
        Log.infof("Running importers for server %s with configuration %s.",
                configuration.getServer(), configuration.getConfigDirectory());
        importers.stream()
                .sorted(Comparator.comparingInt(AbstractImporter::getPriority))
                .forEach(importer -> importer.runImport(entityStore));
    }
}
