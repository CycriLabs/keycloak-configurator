package com.cycrilabs.keycloak.configurator.commands.configure.control;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigureCommandConfiguration;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

/**
 * Load configuration files from the file system by reading all configuration files from the
 * configured directory. The structure of the directory should be as follows:
 *
 * <pre>
 * ├── configuration
 * │   ├── realm-a
 * │   │   ├── realms_realm-a.json
 * │   │   ├── clients_client-a.json
 * │   │   ├── client-roles_client-a_role-a.json
 * │   │   ├── realm-roles_realm-role-a.json
 * │   │   ├── service-account-client-roles_client-a_role-a.json
 * │   │   ├── groups_group-a.json
 * │   │   ├── users_user-a.json
 * │   │   ├── service-account-client-roles_client-a_realm-role-a.json
 * │   │   ├── components_component-a.json
 * │   ├── realm-b
 * │   │   ├── realms_realm-b.json
 * │   │   ├── clients_client-b.json
 * │   │   ├── ...
 * </pre>
 *
 * Based on the naming of the directories, the loader will map the files to the corresponding entity
 * types.
 */
public class FlatFileConfigurationFileLoader extends ConfigurationFileLoader {
    private static final String TYPE_NAME_SEPARATOR = "_";

    private final ConfigureCommandConfiguration configuration;
    private final Map<EntityType, List<ConfigurationFile>> configurationFiles = new HashMap<>();

    public FlatFileConfigurationFileLoader(final ConfigureCommandConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Map<EntityType, List<ConfigurationFile>> create() {
        final String configurationDirectory = configuration.getConfigDirectory();
        final Path configurationPath = Paths.get(configurationDirectory).toAbsolutePath();
        Log.infof("Creating configuration file store for directory '%s'.", configurationPath);

        final List<Path> allRealmDirectories = listDirectoriesInPath(configurationPath);
        for (final Path realmConfiguration : allRealmDirectories) {
            mapEntityTypes(listFilesInPath(realmConfiguration, false));
        }

        return configurationFiles;
    }

    /**
     * Map all entity types to their configuration files.
     *
     * @param filePaths
     *         list of all files in a realm directory
     */
    private void mapEntityTypes(final List<Path> filePaths) {
        for (final Path filePath : filePaths) {
            final String fileName = filePath.getName(filePath.getNameCount() - 1).toString();
            final String[] fileNameParts = fileName.split(TYPE_NAME_SEPARATOR);
            if (fileNameParts.length < 2) {
                Log.errorf("Could not determine entity type from file '%s'.", filePath);
                continue;
            }

            for (final EntityType entityType : EntityType.values()) {
                Log.debugf("Reading file from '%s' for type '%s'.", filePath, entityType);
                if (compareDirectoryNames(fileNameParts[0], entityType)) {
                    Log.infof("Found entity type '%s' in directory '%s'.", entityType, filePath);
                    configurationFiles.computeIfAbsent(entityType, k -> new ArrayList<>())
                            .add(createConfigurationFile(entityType, filePath));
                    break;
                } else {
                    Log.debugf("Skipping file '%s' for type '%s'.", filePath, entityType);
                }
            }
        }
    }

    private ConfigurationFile createConfigurationFile(final EntityType entityType,
            final Path filePath) {
        final String realmName = filePath.getName(filePath.getNameCount() - 2).toString();
        String clientId = null;
        String serviceUsername = null;

        Log.infof("filename %s", filePath.getName(filePath.getNameCount() - 1));
        final String[] fileNameParts = filePath.getName(filePath.getNameCount() - 1)
                .toString()
                .split(TYPE_NAME_SEPARATOR);
        switch (entityType) {
            case REALM:
            case CLIENT:
            case REALM_ROLE:
            case GROUP:
            case USER:
            case COMPONENT:
                break;
            case CLIENT_ROLE:
                clientId = fileNameParts[1];
                break;
            case SERVICE_ACCOUNT_REALM_ROLE:
            case SERVICE_ACCOUNT_CLIENT_ROLE:
                serviceUsername = fileNameParts[1];
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
