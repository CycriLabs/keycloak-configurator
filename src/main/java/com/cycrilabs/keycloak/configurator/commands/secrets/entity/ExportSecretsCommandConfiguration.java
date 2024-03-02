package com.cycrilabs.keycloak.configurator.commands.secrets.entity;

import lombok.Getter;

import com.cycrilabs.keycloak.configurator.shared.entity.KeycloakConfiguration;

import picocli.CommandLine.ParseResult;

@Getter
public class ExportSecretsCommandConfiguration extends KeycloakConfiguration {
    private final String realmName;
    private final String configDirectory;
    private final String clientIds;
    private final String outputDirectory;

    public ExportSecretsCommandConfiguration(final ParseResult parseResult) {
        super(parseResult);
        realmName = getMatchedOption(parseResult, "-r");
        configDirectory = getMatchedOption(parseResult, "-c");
        clientIds = getMatchedOption(parseResult, "-n");
        outputDirectory = getMatchedOption(parseResult, "-o");
    }
}
