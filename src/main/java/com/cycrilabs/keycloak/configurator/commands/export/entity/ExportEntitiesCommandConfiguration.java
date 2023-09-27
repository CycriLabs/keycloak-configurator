package com.cycrilabs.keycloak.configurator.commands.export.entity;

import lombok.Getter;

import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;
import com.cycrilabs.keycloak.configurator.shared.entity.KeycloakConfiguration;

import picocli.CommandLine.ParseResult;

@Getter
public class ExportEntitiesCommandConfiguration extends KeycloakConfiguration {
    private final String realmName;
    private final String client;
    private final EntityType entityType;
    private final String entityName;
    private final String outputDirectory;

    public ExportEntitiesCommandConfiguration(final ParseResult parseResult) {
        super(parseResult);
        realmName = getMatchedOption(parseResult, "-r");
        client = getMatchedOption(parseResult, "-c");
        entityType = EntityType.fromName(getMatchedOption(parseResult, "-t"));
        entityName = getMatchedOption(parseResult, "-n");
        outputDirectory = getMatchedOption(parseResult, "-o");
    }
}
