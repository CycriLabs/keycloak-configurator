package com.cycrilabs.keycloak.configurator.commands.diff.entity;

/**
 * Thrown when the Keycloak server cannot be reached or rejects the given credentials. This is
 * reported separately from a configuration difference, because an unreachable server would
 * otherwise make every configured entity look as if it did not exist.
 */
public class ServerUnreachableException extends RuntimeException {
    public ServerUnreachableException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
