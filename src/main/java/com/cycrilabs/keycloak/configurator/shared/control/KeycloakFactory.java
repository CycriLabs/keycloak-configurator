package com.cycrilabs.keycloak.configurator.shared.control;

import lombok.NoArgsConstructor;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

import com.cycrilabs.keycloak.configurator.shared.entity.KeycloakConfiguration;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class KeycloakFactory {
    private static final String REALM_MASTER = "master";
    private static final String CLIENT_ID_ADMIN_CLI = "admin-cli";

    public static Keycloak create(final KeycloakConfiguration configuration) {
        return KeycloakBuilder.builder()
                .serverUrl(configuration.getServer())
                .realm(REALM_MASTER)
                .clientId(CLIENT_ID_ADMIN_CLI)
                .grantType(OAuth2Constants.PASSWORD)
                .username(configuration.getUsername())
                .password(configuration.getPassword())
                .build();
    }
}
