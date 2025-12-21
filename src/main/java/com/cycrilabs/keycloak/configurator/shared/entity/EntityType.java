package com.cycrilabs.keycloak.configurator.shared.entity;

import java.util.Arrays;

import lombok.Getter;

@Getter
public enum EntityType {
    REALM(1, "realm", "realms"),
    CLIENT_SCOPE(2, "client-scope", "client-scopes"),
    CLIENT(3, "client", "clients"),
    CLIENT_ROLE(4, "client-role", "client-roles"),
    REALM_ROLE(5, "realm-role", "realm-roles"),
    SERVICE_ACCOUNT_CLIENT_ROLE(6, "service-account-client-role", "service-account-client-roles"),
    GROUP(7, "group", "groups"),
    USER(8, "user", "users"),
    SERVICE_ACCOUNT_REALM_ROLE(9, "service-account-realm-role", "service-account-realm-roles"),
    COMPONENT(10, "component", "components");

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
