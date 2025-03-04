package com.cycrilabs.keycloak.configurator.commands.configure.entity;

import lombok.Getter;

import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;
import com.cycrilabs.keycloak.configurator.shared.entity.KeycloakConfiguration;

import picocli.CommandLine.ParseResult;

@Getter
public class ConfigureCommandConfiguration extends KeycloakConfiguration {
    private final String configDirectory;
    private final EntityType entityType;
    private final boolean exitOnError;

    public ConfigureCommandConfiguration(final ParseResult parseResult) {
        super(parseResult);
        configDirectory = getMatchedOption(parseResult, "-c");
        entityType = EntityType.fromName(getMatchedOption(parseResult, "-t"));
        exitOnError = this.<Boolean>getMatchedOption(parseResult, "--exit-on-error").booleanValue();
    }
}
