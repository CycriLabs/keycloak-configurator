package com.cycrilabs.keycloak.configurator.shared.entity;

import lombok.Getter;

import picocli.CommandLine.ParseResult;

@Getter
public abstract class KeycloakConfiguration {
    private String server;
    private String username;
    private String password;

    protected KeycloakConfiguration() {
        // required to avoid "No default constructor for class" error
    }

    protected KeycloakConfiguration(final ParseResult parseResult) {
        this.server = getMatchedOption(parseResult, "-s");
        this.username = getMatchedOption(parseResult, "-u");
        this.password = getMatchedOption(parseResult, "-p");
    }

    protected String getMatchedOption(final ParseResult parseResult, final String name) {
        return parseResult.subcommand().commandSpec().optionsMap().get(name).getValue();
    }
}

