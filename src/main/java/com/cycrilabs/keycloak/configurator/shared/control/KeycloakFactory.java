package com.cycrilabs.keycloak.configurator.shared.control;

import lombok.NoArgsConstructor;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

import com.cycrilabs.keycloak.configurator.shared.entity.KeycloakConfiguration;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class KeycloakFactory {
    public static Keycloak create(final KeycloakConfiguration configuration) {
        return KeycloakBuilder.builder()
                .serverUrl(configuration.getServer())
                .realm("master")
                .clientId("admin-cli")
                .grantType(OAuth2Constants.PASSWORD)
                .username(configuration.getUsername())
                .password(configuration.getPassword())
                .build();
    }
}
