package com.cycrilabs.keycloak.configurator.commands.secrets.entity;

import lombok.Getter;

import com.cycrilabs.keycloak.configurator.shared.entity.KeycloakConfiguration;

import picocli.CommandLine.ParseResult;

@Getter
public class ExportSecretsCommandConfiguration extends KeycloakConfiguration {
    private String realmName;
    private String configDirectory;
    private String clientIds;
    private String outputDirectory;

    public ExportSecretsCommandConfiguration() {
        // required to avoid "No default constructor for class" error
    }

    public ExportSecretsCommandConfiguration(final ParseResult parseResult) {
        super(parseResult);
        realmName = getMatchedOption(parseResult, "-r");
        configDirectory = getMatchedOption(parseResult, "-c");
        clientIds = getMatchedOption(parseResult, "-n");
        outputDirectory = getMatchedOption(parseResult, "-o");
    }
}
