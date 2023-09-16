package com.cycrilabs.keycloak.configurator.shared.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import com.cycrilabs.keycloak.configurator.shared.entity.KeycloakConfiguration;

import picocli.CommandLine;

@ApplicationScoped
public class KeycloakConfigurationProducer {
    @Produces
    @ApplicationScoped
    KeycloakConfiguration createConfiguration(final CommandLine.ParseResult parseResult) {
        return new KeycloakConfiguration(
                parseResult.matchedOption("s").getValue().toString(),
                parseResult.matchedOption("u").getValue().toString(),
                parseResult.matchedOption("p").getValue().toString()
        );
    }
}
