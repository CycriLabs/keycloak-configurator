package com.cycrilabs.keycloak.configurator.commands.configure.control;

import java.util.Comparator;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import com.cycrilabs.keycloak.configurator.commands.configure.boundary.AbstractImporter;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigureCommandConfiguration;

import io.quarkus.logging.Log;

@ApplicationScoped
public class ImportRunner {
    @Inject
    ConfigureCommandConfiguration configuration;
    @Inject
    Instance<AbstractImporter> importers;

    public void run() {
        Log.infof("Running importers for server %s with configuration %s.",
                configuration.getServer(), configuration.getConfigDirectory());
        importers.stream()
                .sorted(Comparator.comparingInt(AbstractImporter::getPriority))
                .forEach(AbstractImporter::runImport);
    }
}
