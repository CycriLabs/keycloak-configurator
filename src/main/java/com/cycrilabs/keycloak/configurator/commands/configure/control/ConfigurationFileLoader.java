package com.cycrilabs.keycloak.configurator.commands.configure.control;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

/**
 * Load configuration files from the file system.
 */
public abstract class ConfigurationFileLoader {
    /**
     * The path separator for the current operating system. This is used to split the file path into
     * its parts. This is used in a regular expression, so special characters need to be escaped.
     */
    public static final String PATH_SEPARATOR =
            Pattern.quote(FileSystems.getDefault().getSeparator());

    /**
     * Create a map of entity types to their configuration files.
     *
     * @return the map of entity types to their configuration files
     */
    public abstract Map<EntityType, List<ConfigurationFile>> create();

    /**
     * List all directories in the given directory.
     *
     * @param dir
     *         directory to list directories in
     * @return list of directories in the given directory
     */
    protected List<Path> listDirectoriesInPath(final Path dir) {
        try (final Stream<Path> stream = Files.list(dir)) {
            return stream
                    .filter(Files::isDirectory)
                    .toList();
        } catch (final IOException e) {
            Log.errorf(e, "Could not read directory '%s'", dir);
        }
        return Collections.emptyList();
    }

    /**
     * List all files in the given directory and its subdirectories that end with '.json'.
     *
     * @param dir
     *         directory to list files in
     * @return list of files in the given directory and its subdirectories
     */
    protected List<Path> listFilesInPath(final Path dir) {
        return listFilesInPath(dir, true);
    }

    /**
     * List all files in the given directory and its subdirectories that end with '.json'.
     *
     * @param dir
     *         directory to list files in
     * @return list of files in the given directory and its subdirectories
     */
    protected List<Path> listFilesInPath(final Path dir, final boolean recursive) {
        try (final Stream<Path> stream = Files.walk(dir, recursive ? Integer.MAX_VALUE : 1)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(file -> file.toString().endsWith(".json"))
                    .sorted()
                    .toList();
        } catch (final IOException e) {
            Log.errorf(e, "Could not read directory '%s'", dir);
        }
        return Collections.emptyList();
    }

    /**
     * Compare the given value with the directory name of the given entity type.
     *
     * @param potentialEntityType
     *         value to compare
     * @param entityType
     *         entity type to compare
     * @return true if the value contains the directory name of the entity type
     */
    protected boolean compareDirectoryNames(final String potentialEntityType,
            final EntityType entityType) {
        // some simple algorithm to compare if a string value matches an entity type,
        // e.g. if it is prefixed by number: 1_realms and realms
        // the overall length difference should not be that huge to avoid embedded naming
        // e.g. client-roles and service-account-client-roles
        final int maxNameLengthDiff = 5;
        final boolean contains = potentialEntityType.contains(entityType.getDirectory());
        return contains && potentialEntityType.length() - entityType.getDirectory().length()
                < maxNameLengthDiff;
    }
}
