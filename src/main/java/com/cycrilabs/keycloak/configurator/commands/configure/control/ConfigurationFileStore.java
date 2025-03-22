package com.cycrilabs.keycloak.configurator.commands.configure.control;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

/**
 * Store for configuration files that holds a list of all configuration files for each entity type.
 * the configuration files are read from the configured directory and its subdirectories on first
 * access.
 */
@ApplicationScoped
public class ConfigurationFileStore {
    @Inject
    ConfigurationFileLoader loader;

    private final Map<EntityType, List<ConfigurationFile>> configurationFiles = new HashMap<>();

    public void init() {
        Log.infof("Initializing configuration file store.");
        configurationFiles.putAll(loader.create());
    }

    /**
     * Get all paths for the given entity type.
     *
     * @param type
     *         entity type
     * @return list of files for the given entity type
     */
    public List<ConfigurationFile> getImportFiles(final EntityType type) {
        return configurationFiles.getOrDefault(type, Collections.emptyList());
    }
}
