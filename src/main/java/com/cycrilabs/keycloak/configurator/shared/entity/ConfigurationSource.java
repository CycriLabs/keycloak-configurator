package com.cycrilabs.keycloak.configurator.shared.entity;

/**
 * Describes where local configuration files are read from and in which layout they are
 * organized. Implemented by all command configurations that read the configuration-as-code
 * files, so that the loading infrastructure does not depend on a concrete command.
 */
public interface ConfigurationSource {
    /**
     * The directory containing the configuration files.
     *
     * @return path to the configuration directory
     */
    String getConfigDirectory();

    /**
     * Whether the configuration files are organized as a flat file list instead of nested
     * entity type directories.
     *
     * @return true if a flat file layout is used
     */
    boolean isFlatFiles();
}
