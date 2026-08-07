package com.cycrilabs.keycloak.configurator.commands.diff.entity;

/**
 * The kind of difference detected for a single field of an entity.
 */
public enum FieldDifferenceKind {
    /**
     * The field is declared in the local configuration but not set on the server.
     */
    MISSING_ON_SERVER,
    /**
     * The field is set on both sides, but the values differ.
     */
    VALUE_DIFFERENT,
    /**
     * Elements of a declared collection that are only present in the local configuration.
     */
    ONLY_IN_LOCAL,
    /**
     * Elements of a declared collection that are only present on the server.
     */
    ONLY_ON_SERVER
}
