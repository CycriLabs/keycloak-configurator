package com.cycrilabs.keycloak.configurator.commands.diff.entity;

/**
 * The kind of difference detected for an entity.
 */
public enum DifferenceKind {
    /**
     * The entity is configured locally but does not exist on the server.
     */
    MISSING_ON_SERVER,
    /**
     * The entity exists on both sides, but at least one locally declared field differs.
     */
    FIELD_DIFFERENT,
    /**
     * The entity exists on the server but is not configured locally.
     */
    EXTRA_ON_SERVER
}
