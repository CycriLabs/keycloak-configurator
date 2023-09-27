package com.cycrilabs.keycloak.configurator.shared.entity;

import lombok.Getter;

@Getter
public enum EntityType {
    REALM(1, "realm", "realms"),
    CLIENT(2, "client", "clients"),
    CLIENT_ROLE(3, "client-role", "client-roles"),
    REALM_ROLE(4, "realm-role", "realm-roles"),
    GROUP(5, "group", "groups"),
    USER(6, "user", "users");

    private final int priority;
    private final String name;
    private final String directory;

    EntityType(final int priority, final String name, final String directory) {
        this.priority = priority;
        this.name = name;
        this.directory = directory;
    }

    public static EntityType fromName(final String name) {
        for (final EntityType entityType : EntityType.values()) {
            if (entityType.getName().equals(name)) {
                return entityType;
            }
        }
        throw new IllegalArgumentException("Unknown entity type: " + name);
    }
}
