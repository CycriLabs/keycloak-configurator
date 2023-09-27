package com.cycrilabs.keycloak.configurator.commands.export.control;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import com.cycrilabs.keycloak.configurator.commands.export.boundary.AbstractExporter;
import com.cycrilabs.keycloak.configurator.commands.export.entity.ExportEntitiesCommandConfiguration;
import com.cycrilabs.keycloak.configurator.shared.control.KeycloakOptions;

import io.quarkus.logging.Log;
import picocli.CommandLine;

@CommandLine.Command(name = "export-entities", mixinStandardHelpOptions = true)
public class ExportEntitiesCommand implements Runnable {
    @CommandLine.Mixin
    KeycloakOptions keycloakOptions;
    @CommandLine.Option(names = { "-r", "--realm" },
            description = "Realm name to export entities from.")
    String realm;
    @CommandLine.Option(names = { "-c", "--client" },
            description = "Client name to export entities from.")
    String client;
    @CommandLine.Option(names = { "-t", "--entity-type" },
            description = "Entity type to export. If not provided, all entities of the realm & client are exported.")
    String entityType;
    @CommandLine.Option(names = { "-n", "--entity-name" },
            description = "Name of the entity to export. If not provided, all entities of the given type are exported.")
    String entityName;
    @CommandLine.Option(names = { "-o", "--output" }, defaultValue = "./",
            description = "Output directory for generate files.")
    String outputDirectory;

    @Inject
    ExportEntitiesCommandConfiguration configuration;
    @Inject
    Instance<AbstractExporter> exporters;

    @Override
    public void run() {
        Log.infof("Exporting entities from realm '%s' of type '%s'.",
                configuration.getRealmName(), configuration.getEntityType().getName());
        exporters.stream()
                .forEach(AbstractExporter::export);
    }
}
