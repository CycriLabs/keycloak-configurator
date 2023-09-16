package com.cycrilabs.keycloak.configurator.shared.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class KeycloakConfiguration {
    String server;
    String username;
    String password;
}
