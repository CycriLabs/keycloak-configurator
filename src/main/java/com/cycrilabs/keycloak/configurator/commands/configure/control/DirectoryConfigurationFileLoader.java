package com.cycrilabs.keycloak.configurator.commands.configure.control;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigureCommandConfiguration;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

/**
 * Load configuration files from the file system by reading all configuration files from the
 * configured directory and its subdirectories. The structure of the directory should be as follows:
 *
 * <pre>
 * ├── configuration
 * │   ├── realm-a
 * │   │   ├── realms
 * │   │   │   ├── realm-a.json
 * │   │   ├── clients
 * │   │   │   ├── client-a.json
 * │   │   ├── client-roles
 * │   │   │   ├── client-a
 * │   │   │   │   ├── role-a.json
 * │   │   ├── realm-roles
 * │   │   │   ├── realm-role-a.json
 * │   │   ├── service-account-client-roles
 * │   │   │   ├── client-a
 * │   │   │   │   ├── role-a.json
 * │   │   ├── groups
 * │   │   │   ├── ...
 * │   │   ├── users
 * │   │   │   ├── ...
 * │   │   ├── service-account-client-roles
 * │   │   │   ├── client-a
 * │   │   │   │   ├── realm-role-a.json
 * │   │   ├── components
 * │   │   │   ├── ...
 * │   ├── realm-b
 * │   │   ├── realms
 * │   │   │   ├── realm-b.json
 * │   │   ├── clients
 * │   │   ├── ...
 * </pre>
 *
 * Based on the naming of the directories, the loader will map the files to the corresponding entity
 * types.
 */
public class DirectoryConfigurationFileLoader extends ConfigurationFileLoader {
    private final ConfigureCommandConfiguration configuration;
    private final Map<EntityType, List<ConfigurationFile>> configurationFiles = new HashMap<>();

    public DirectoryConfigurationFileLoader(final ConfigureCommandConfiguration configuration) {
        this.configuration = configuration;
    }

    /**
     * Load the configuration files by reading all configuration files from the configured
     * directory and its subdirectories.
     */
    @Override
    public Map<EntityType, List<ConfigurationFile>> create() {
        final String configurationDirectory = configuration.getConfigDirectory();
        final Path configurationPath = Paths.get(configurationDirectory).toAbsolutePath();
        Log.infof("Creating configuration file store for directory '%s'.", configurationPath);

        final List<Path> allRealmDirectories = listDirectoriesInPath(configurationPath);
        for (final Path realmConfiguration : allRealmDirectories) {
            mapEntityTypes(createTypeDirectoryLookup(realmConfiguration));
        }
        return configurationFiles;
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

                    configurationFiles.compute(entityType, (key, value) -> {
                        final List<ConfigurationFile> filesInPath =
                                listFilesInPath(fullEntityTypePath)
                                        .stream()
                                        .map(filePath -> createConfigurationFile(entityType,
                                                filePath))
                                        .toList();
                        return value == null
                               ? filesInPath
                               : Stream.concat(value.stream(), filesInPath.stream())
                                       .toList();
                    });
                    Log.debugf("Found %d files for type '%s'.",
                            configurationFiles.get(entityType).size(), entityType);
                } else {
                    Log.debugf("Skipping directory '%s' for type '%s'.", fullEntityTypePath,
                            entityType);
                }
            }
        }
    }

    private ConfigurationFile createConfigurationFile(final EntityType entityType,
            final Path filePath) {
        String realmName = null;
        String clientId = null;
        String serviceUsername = null;

        final String[] fileNameParts = filePath.toString().split(PATH_SEPARATOR);
        switch (entityType) {
            case REALM:
                break;
            case CLIENT:
            case REALM_ROLE:
            case GROUP:
            case USER:
            case COMPONENT:
                realmName = fileNameParts[fileNameParts.length - 3];
                break;
            case CLIENT_ROLE:
                realmName = fileNameParts[fileNameParts.length - 4];
                clientId = fileNameParts[fileNameParts.length - 2];
                break;
            case SERVICE_ACCOUNT_REALM_ROLE:
            case SERVICE_ACCOUNT_CLIENT_ROLE:
                realmName = fileNameParts[fileNameParts.length - 4];
                serviceUsername = fileNameParts[fileNameParts.length - 2];
                break;
        }

        return ConfigurationFile.builder()
                .file(filePath)
                .realmName(realmName)
                .clientId(clientId)
                .serviceUsername(serviceUsername)
                .build();
    }
}
