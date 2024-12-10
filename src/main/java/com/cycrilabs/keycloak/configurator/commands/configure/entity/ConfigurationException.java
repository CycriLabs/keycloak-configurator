package com.cycrilabs.keycloak.configurator.commands.configure.entity;

/**
 * Exception that is thrown when a configuration error occurs. This is exception is bubbled up to
 * the {@link com.cycrilabs.keycloak.configurator.commands.configure.control.ImportRunner} and stops
 * the configuration process.
 */
public class ConfigurationException extends Exception {
    public ConfigurationException(final String message) {
        super(message);
    }
}
