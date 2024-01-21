package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.keycloak.admin.client.Keycloak;

import com.cycrilabs.keycloak.configurator.commands.configure.control.ConfigurationFileStore;
import com.cycrilabs.keycloak.configurator.commands.configure.control.EntityStore;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigureCommandConfiguration;
import com.cycrilabs.keycloak.configurator.shared.control.KeycloakFactory;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

public abstract class AbstractImporter {
    /**
     * The path separator for the current operating system. This is used to split the file path into
     * its parts. This is used in a regular expression, so special characters need to be escaped.
     */
    public static final String PATH_SEPARATOR =
            Pattern.quote(FileSystems.getDefault().getSeparator());

    @Inject
    protected ConfigureCommandConfiguration configuration;
    @Inject
    protected ConfigurationFileStore configurationFileStore;
    @Inject
    protected EntityStore entityStore;

    protected Keycloak keycloak;

    @PostConstruct
    public void init() {
        keycloak = KeycloakFactory.create(configuration);
    }

    public void runImport() {
        if (configuration.getEntityType() != null && configuration.getEntityType() != getType()) {
            Log.infof("Skipping importer '%s' for entity type '%s'.", getClass().getSimpleName(),
                    configuration.getEntityType());
            return;
        }

        Log.infof("Executing importer '%s'.", getClass().getSimpleName());
        for (final Path importFile : getImportFiles()) {
            Log.debugf("Importing file '%s'.", importFile);
            importFile(importFile);
        }
    }

    private List<Path> getImportFiles() {
        final List<Path> importFiles = configurationFileStore.getImportFiles(getType());
        if (importFiles.isEmpty()) {
            Log.infof("No files found for importer '%s'.", getClass().getSimpleName());
        }
        return importFiles;
    }

    public int getPriority() {
        return getType().getPriority();
    }

    public abstract EntityType getType();

    protected abstract Object importFile(final Path file);
}
