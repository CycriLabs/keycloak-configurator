package com.cycrilabs.keycloak.configurator.commands.generate.boundary;

import java.util.List;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;

import com.cycrilabs.keycloak.configurator.commands.generate.entity.GenerateSecretsCommandConfiguration;

import io.quarkus.logging.Log;

@ApplicationScoped
public class GenerateSecrets {
    @Inject
    GenerateSecretsCommandConfiguration configuration;
    @Inject
    Keycloak keycloak;

    public void run() {
        final List<String> generatedIds = getClients()
                .filter(client -> client.getSecret() != null)
                .map(ClientRepresentation::getId)
                .map(this::generateSecret)
                .toList();
        Log.infof("Generated secrets for %d clients.", Integer.valueOf(generatedIds.size()));
    }

    private Stream<ClientRepresentation> getClients() {
        return configuration.getClientId() == null
                ? keycloak.realm(configuration.getRealmName())
                .clients()
                .findAll()
                .stream()
                : keycloak.realm(configuration.getRealmName())
                .clients()
                .findByClientId(configuration.getClientId())
                .stream();
    }

    private String generateSecret(final String id) {
        return keycloak.realm(configuration.getRealmName())
                .clients()
                .get(id)
                .generateNewSecret()
                .getId();
    }
}
