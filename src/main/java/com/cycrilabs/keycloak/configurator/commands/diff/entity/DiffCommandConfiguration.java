package com.cycrilabs.keycloak.configurator.commands.diff.entity;

import java.util.List;

import lombok.Getter;

import com.cycrilabs.keycloak.configurator.shared.entity.ConfigurationSource;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;
import com.cycrilabs.keycloak.configurator.shared.entity.KeycloakConfiguration;

import picocli.CommandLine.ParseResult;

@Getter
public class DiffCommandConfiguration extends KeycloakConfiguration
        implements ConfigurationSource {
    private String configDirectory;
    private EntityType entityType;
    private boolean flatFiles;
    private boolean includeBuiltIns;
    private List<String> ignoredExtras = List.of();

    public DiffCommandConfiguration() {
        // required to avoid "No default constructor for class" error
    }

    public DiffCommandConfiguration(final ParseResult parseResult) {
        super(parseResult);
        configDirectory = getMatchedOption(parseResult, "-c");
        entityType = getMatchedOption(parseResult, "-t");
        flatFiles = this.<Boolean>getMatchedOption(parseResult, "--flat-files").booleanValue();
        includeBuiltIns =
                this.<Boolean>getMatchedOption(parseResult, "--include-built-ins").booleanValue();
        final List<String> configuredIgnores = getMatchedOption(parseResult, "--ignore-extra");
        ignoredExtras = configuredIgnores == null
                        ? List.of()
                        : List.copyOf(configuredIgnores);
    }
}
