package com.cycrilabs.keycloak.configurator.commands.export.boundary;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.keycloak.admin.client.Keycloak;

import com.cycrilabs.keycloak.configurator.commands.export.entity.ExportEntitiesCommandConfiguration;
import com.cycrilabs.keycloak.configurator.shared.control.KeycloakFactory;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

public abstract class AbstractExporter {
    @Inject
    protected ExportEntitiesCommandConfiguration configuration;
    protected Keycloak keycloak;

    @PostConstruct
    public void init() {
        keycloak = KeycloakFactory.create(configuration);
    }

    public void writeFile(final String fileContent, final String name, final String realm) {
        writeFile(fileContent, name, realm, "");
    }

    public void writeFile(final String fileContent, final String name, final String realm,
            final String client) {
        final Path targetFile =
                Path.of(configuration.getOutputDirectory(), getType().getDirectory(), realm, client,
                        name + ".json");
        try {
            Files.createDirectories(targetFile.getParent());
            Files.writeString(targetFile, fileContent, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            Log.errorf(e, "Failed to write file '%s'.", targetFile.toString());
        }
    }

    public void export() {
        Log.infof("Executing exporter '%s'.", getClass().getSimpleName());

        if (configuration.getEntityType() != null && configuration.getEntityType() != getType()) {
            Log.infof("Skipping exporter '%s' for export type '%s'.", getClass().getSimpleName(),
                    configuration.getEntityType());
            return;
        }

        if (configuration.getEntityName() != null) {
            exportEntity(configuration.getEntityName());
        } else {
            exportEntities();
        }
    }

    public abstract EntityType getType();

    protected abstract void exportEntity(String entityName);

    protected abstract void exportEntities();
}
