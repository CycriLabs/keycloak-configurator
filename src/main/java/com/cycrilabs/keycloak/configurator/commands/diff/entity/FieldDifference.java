package com.cycrilabs.keycloak.configurator.commands.diff.entity;

/**
 * A difference detected for a single field of an entity.
 *
 * @param path
 *         the path of the field within the entity, e.g. {@code config.bindCredential}
 * @param kind
 *         the kind of difference
 * @param localValue
 *         the rendered local value, or the elements only present locally
 * @param serverValue
 *         the rendered server value, or the elements only present on the server
 */
public record FieldDifference(
        String path,
        FieldDifferenceKind kind,
        String localValue,
        String serverValue
) {
}
