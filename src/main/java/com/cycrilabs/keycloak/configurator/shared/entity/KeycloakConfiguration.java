package com.cycrilabs.keycloak.configurator.shared.entity;

import lombok.Getter;

import picocli.CommandLine.ParseResult;

@Getter
public abstract class KeycloakConfiguration {
    private String server;
    private String username;
    private String password;
    private boolean dryRun;

    protected KeycloakConfiguration() {
        // required to avoid "No default constructor for class" error
    }

    protected KeycloakConfiguration(final ParseResult parseResult) {
        server = getMatchedOption(parseResult, "-s");
        username = getMatchedOption(parseResult, "-u");
        password = getMatchedOption(parseResult, "-p");
        dryRun = this.<Boolean>getMatchedOption(parseResult, "--dry-run").booleanValue();
    }

    protected <T> T getMatchedOption(final ParseResult parseResult, final String name) {
        return parseResult.subcommand().commandSpec().optionsMap().get(name).getValue();
    }
}

