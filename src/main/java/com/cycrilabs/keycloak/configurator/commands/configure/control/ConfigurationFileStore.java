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

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigureCommandConfiguration;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class ConfigurationFileStore {
    @Inject
    ConfigureCommandConfiguration configuration;

    private final Map<EntityType, List<Path>> configurationFiles = new HashMap<>();

    @PostConstruct
    public void init() {
        Log.infof("Initializing configuration file store.");
        create();
    }

    private void create() {
        final String configurationDirectory = configuration.getConfigDirectory();
        final Path configurationPath = Paths.get(configurationDirectory).toAbsolutePath();
        Log.infof("Creating configuration file store for directory '%s'.", configurationPath);

        final List<Path> allRealmDirectories = listDirectoriesInPath(configurationPath);
        for (final Path realmConfiguration : allRealmDirectories) {
            final Map<String, Path> realmConfigurationDirectories =
                    listDirectoriesInPath(realmConfiguration)
                            .stream()
                            .collect(Collectors.toMap(
                                    path -> path.getName(path.getNameCount() - 1).toString(),
                                    Function.identity()));
            for (final EntityType entityType : EntityType.values()) {
                for (final Map.Entry<String, Path> configurationEntry : realmConfigurationDirectories.entrySet()) {
                    Log.debugf("Reading files from '%s' for type '%s'.",
                            configurationEntry.getValue(), entityType);
                    if (configurationEntry.getKey().contains(entityType.getDirectory())) {
                        configurationFiles.computeIfAbsent(entityType,
                                key -> listFilesInPath(configurationEntry.getValue()));
                    }
                }
            }
        }
    }

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

    private List<Path> listFilesInPath(final Path dir) {
        try (final Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".json"))
                    .toList();
        } catch (final IOException e) {
            Log.errorf("Could not read directory '%s'.", dir);
        }
        return Collections.emptyList();
    }

    public List<Path> getImportFiles(final EntityType type) {
        return configurationFiles.getOrDefault(type, Collections.emptyList());
    }
}
