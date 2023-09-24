package com.cycrilabs.keycloak.configurator.commands.configure.entity;

import lombok.Getter;

import com.cycrilabs.keycloak.configurator.shared.entity.KeycloakConfiguration;

import picocli.CommandLine.ParseResult;

@Getter
public class ConfigureCommandConfiguration extends KeycloakConfiguration {
    private final String configDirectory;

    public ConfigureCommandConfiguration(final ParseResult parseResult) {
        super(parseResult);
        configDirectory = getMatchedOption(parseResult, "-c");
    }
}
