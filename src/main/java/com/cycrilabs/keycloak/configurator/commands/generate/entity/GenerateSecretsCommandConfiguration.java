package com.cycrilabs.keycloak.configurator.commands.generate.entity;

import lombok.Getter;

import com.cycrilabs.keycloak.configurator.shared.entity.KeycloakConfiguration;

import picocli.CommandLine.ParseResult;

@Getter
public class GenerateSecretsCommandConfiguration extends KeycloakConfiguration {
    private final String realmName;
    private final String clientId;

    public GenerateSecretsCommandConfiguration(final ParseResult parseResult) {
        super(parseResult);
        realmName = getMatchedOption(parseResult, "-r");
        clientId = getMatchedOption(parseResult, "-c");
    }
}
