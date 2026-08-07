package com.cycrilabs.keycloak.configurator.commands.diff.entity;

import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

/**
 * A single reported difference between the server and the local configuration.
 *
 * @param entityType
 *         the type of the affected entity
 * @param realm
 *         the realm the entity belongs to
 * @param entity
 *         the identifying name of the entity, e.g. {@code blog} or {@code blog:posts-create}
 * @param kind
 *         the kind of difference
 * @param field
 *         the affected field, only set for {@link DifferenceKind#FIELD_DIFFERENT}
 */
public record Difference(
        EntityType entityType,
        String realm,
        String entity,
        DifferenceKind kind,
        FieldDifference field
) {
    public static Difference missingOnServer(final EntityType entityType, final String realm,
            final String entity) {
        return new Difference(entityType, realm, entity, DifferenceKind.MISSING_ON_SERVER, null);
    }

    public static Difference extraOnServer(final EntityType entityType, final String realm,
            final String entity) {
        return new Difference(entityType, realm, entity, DifferenceKind.EXTRA_ON_SERVER, null);
    }

    public static Difference fieldDifferent(final EntityType entityType, final String realm,
            final String entity, final FieldDifference field) {
        return new Difference(entityType, realm, entity, DifferenceKind.FIELD_DIFFERENT, field);
    }
}
