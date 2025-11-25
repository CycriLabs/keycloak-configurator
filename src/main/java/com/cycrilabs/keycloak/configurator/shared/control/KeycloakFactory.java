package com.cycrilabs.keycloak.configurator.shared.control;

import java.util.concurrent.TimeUnit;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.JacksonProvider;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;

import com.cycrilabs.keycloak.configurator.shared.entity.KeycloakConfiguration;

import picocli.CommandLine;

@ApplicationScoped
public class KeycloakFactory {
    private static final String REALM_MASTER = "master";
    private static final String CLIENT_ID_ADMIN_CLI = "admin-cli";

    @Produces
    @ApplicationScoped
    Keycloak create(final CommandLine.ParseResult parseResult) {
        final KeycloakConfiguration configuration = new KeycloakConfiguration(parseResult);
        return KeycloakBuilder.builder()
                .serverUrl(configuration.getServer())
                .realm(REALM_MASTER)
                .clientId(CLIENT_ID_ADMIN_CLI)
                .grantType(OAuth2Constants.PASSWORD)
                .username(configuration.getUsername())
                .password(configuration.getPassword())
                .resteasyClient(new ResteasyClientBuilderImpl()
                        .connectionPoolSize(20)
                        .readTimeout(5, TimeUnit.SECONDS)
                        .build()
                        .register(JacksonProvider.class, 100))
                .build();
    }
}
