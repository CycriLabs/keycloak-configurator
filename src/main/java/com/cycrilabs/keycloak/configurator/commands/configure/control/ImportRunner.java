package com.cycrilabs.keycloak.configurator.commands.configure.control;

import java.util.Comparator;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import com.cycrilabs.keycloak.configurator.commands.configure.boundary.AbstractImporter;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationException;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigureCommandConfiguration;

import io.quarkus.logging.Log;

@ApplicationScoped
public class ImportRunner {
    private final ConfigureCommandConfiguration configuration;
    private final Instance<AbstractImporter<?>> importers;

    @Inject
    public ImportRunner(
            final ConfigureCommandConfiguration configuration,
            final Instance<AbstractImporter<?>> importers
    ) {
        this.configuration = configuration;
        this.importers = importers;
    }

    public void run() {
        if (configuration.isDryRun()) {
            Log.info("Running in dry-run mode. No changes will be made.");
        }

        try {
            Log.infof("Running importers for server %s with configuration %s.",
                    configuration.getServer(), configuration.getConfigDirectory());
            final List<AbstractImporter<?>> sortedImporters = importers.stream()
                    .sorted(Comparator.comparingInt(AbstractImporter::getPriority))
                    .toList();
            for (final AbstractImporter<?> importer : sortedImporters) {
                importer.runImport();
            }
        } catch (final ConfigurationException e) {
            Log.errorf("Stopping configuration: %s", e.getMessage());
        }
    }
}
