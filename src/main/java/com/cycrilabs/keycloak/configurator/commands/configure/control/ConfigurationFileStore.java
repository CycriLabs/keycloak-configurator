package com.cycrilabs.keycloak.configurator.commands.configure.control;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigureCommandConfiguration;
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
    ConfigureCommandConfiguration configuration;

    private final Map<EntityType, List<Path>> configurationFiles = new HashMap<>();

    public void init() {
        Log.infof("Initializing configuration file store.");
        create();
    }

    /**
     * Create the configuration file store by reading all configuration files from the configured
     * directory and its subdirectories.
     */
    public void create() {
        final String configurationDirectory = configuration.getConfigDirectory();
        final Path configurationPath = Paths.get(configurationDirectory).toAbsolutePath();
        Log.infof("Creating configuration file store for directory '%s'.", configurationPath);

        final List<Path> allRealmDirectories = listDirectoriesInPath(configurationPath);
        for (final Path realmConfiguration : allRealmDirectories) {
            mapEntityTypes(createTypeDirectoryLookup(realmConfiguration));
        }
    }

    /**
     * List all directories in the given directory.
     *
     * @param dir
     *         directory to list directories in
     * @return list of directories in the given directory
     */
    private List<Path> listDirectoriesInPath(final Path dir) {
        try (final Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(Files::isDirectory)
                    .toList();
        } catch (final IOException e) {
            Log.errorf("Could not read directory '%s'.", dir);
        }
        return Collections.emptyList();
    }

    /**
     * Create a lookup map for the given directory that maps all directories within the given one.
     * The key is the name of the directory and the value is the full path to the directory.
     *
     * @param realmConfiguration
     *         directory to create lookup map for
     * @return lookup map for the given directory
     */
    private Map<String, Path> createTypeDirectoryLookup(final Path realmConfiguration) {
        return listDirectoriesInPath(realmConfiguration)
                .stream()
                .collect(Collectors.toMap(
                        path -> path.getName(path.getNameCount() - 1).toString(),
                        Function.identity()));
    }

    /**
     * Map all entity types to their configuration files.
     *
     * @param typeDirectoryLookup
     *         lookup map for the realm configuration directory
     */
    private void mapEntityTypes(final Map<String, Path> typeDirectoryLookup) {
        for (final EntityType entityType : EntityType.values()) {
            for (final Map.Entry<String, Path> configurationEntry : typeDirectoryLookup.entrySet()) {
                final String potentialEntityTypeDirectory = configurationEntry.getKey();
                final Path fullEntityTypePath = configurationEntry.getValue();
                if (compareDirectoryNames(potentialEntityTypeDirectory, entityType)) {
                    Log.debugf("Reading files from '%s' for type '%s'.", fullEntityTypePath,
                            entityType);
                    configurationFiles.compute(entityType, (key, value) -> value == null
                            ? listFilesInPath(fullEntityTypePath)
                            : Stream.concat(value.stream(),
                                    listFilesInPath(fullEntityTypePath).stream()).toList());
                    Log.debugf("Found %d files for type '%s'.",
                            configurationFiles.get(entityType).size(), entityType);
                } else {
                    Log.debugf("Skipping directory '%s' for type '%s'.", fullEntityTypePath,
                            entityType);
                }
            }
        }
    }

    /**
     * Compare the given directory name with the directory name of the given entity type.
     *
     * @param potentialEntityTypeDirectory
     *         directory name to compare
     * @param entityType
     *         entity type to compare
     * @return true if the directory name contains the directory name of the entity type
     */
    private boolean compareDirectoryNames(final String potentialEntityTypeDirectory,
            final EntityType entityType) {
        // some simple algorithm to compare if a directory matches an entity type,
        // e.g. if it is prefixed by number: 1_realms and realms
        // the overall length difference should not be that huge to avoid embedded naming
        // e.g. client-roles and service-account-client-roles
        final int maxNameLengthDiff = 5;
        return potentialEntityTypeDirectory.contains(entityType.getDirectory())
                && potentialEntityTypeDirectory.length() - entityType.getDirectory().length()
                < maxNameLengthDiff;
    }

    /**
     * List all files in the given directory and its subdirectories that end with '.json'.
     *
     * @param dir
     *         directory to list files in
     * @return list of files in the given directory and its subdirectories
     */
    private List<Path> listFilesInPath(final Path dir) {
        try (final Stream<Path> stream = Files.walk(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".json"))
                    .sorted()
                    .toList();
        } catch (final IOException e) {
            Log.errorf("Could not read directory '%s'.", dir);
        }
        return Collections.emptyList();
    }

    /**
     * Get all paths for the given entity type.
     *
     * @param type
     *         entity type
     * @return list of files for the given entity type
     */
    public List<Path> getImportFiles(final EntityType type) {
        return configurationFiles.getOrDefault(type, Collections.emptyList());
    }
}
