package com.cycrilabs.eam.keycloak.configurator.commands.configure.entity;

import lombok.Getter;

@Getter
public enum EntityImportType {
    REALM(1, "realms"),
    CLIENT(2, "clients"),
    CLIENT_ROLE(3, "client-roles"),
    REALM_ROLE(4, "realm-roles"),
    GROUP(5, "groups"),
    USER(6, "users");

    private final int priority;
    private final String directory;

    EntityImportType(final int priority, final String directory) {
        this.priority = priority;
        this.directory = directory;
    }
}
