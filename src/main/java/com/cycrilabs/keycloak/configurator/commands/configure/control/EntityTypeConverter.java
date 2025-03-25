package com.cycrilabs.keycloak.configurator.commands.configure.control;

import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import picocli.CommandLine;

public class EntityTypeConverter implements CommandLine.ITypeConverter<EntityType> {
    @Override
    public EntityType convert(final String value) {
        final EntityType entityType = EntityType.fromName(value);
        if (entityType == null) {
            throw new CommandLine.TypeConversionException(
                    "Invalid entity type '" + value + "' provided");
        }
        return entityType;
    }
}
