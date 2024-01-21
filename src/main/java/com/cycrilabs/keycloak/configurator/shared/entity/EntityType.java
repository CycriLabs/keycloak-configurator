package com.cycrilabs.keycloak.configurator.shared.entity;

import java.util.Arrays;

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
        return Arrays.stream(EntityType.values())
                .filter(entityType -> entityType.getName().equals(name))
                .findFirst()
                .orElse(null);
    }
}
