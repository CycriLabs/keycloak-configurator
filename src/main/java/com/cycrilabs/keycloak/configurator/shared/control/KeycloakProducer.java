package com.cycrilabs.keycloak.configurator.shared.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

import com.cycrilabs.keycloak.configurator.shared.entity.KeycloakConfiguration;

@ApplicationScoped
public class KeycloakProducer {
    @Inject
    KeycloakConfiguration configuration;

    @Produces
    @ApplicationScoped
    Keycloak keycloak() {
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
