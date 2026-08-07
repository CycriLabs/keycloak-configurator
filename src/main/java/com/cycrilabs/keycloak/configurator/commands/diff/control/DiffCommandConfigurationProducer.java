package com.cycrilabs.keycloak.configurator.commands.diff.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import com.cycrilabs.keycloak.configurator.commands.diff.entity.DiffCommandConfiguration;

import picocli.CommandLine;

@ApplicationScoped
public class DiffCommandConfigurationProducer {
    @Produces
    @ApplicationScoped
    DiffCommandConfiguration createConfiguration(final CommandLine.ParseResult parseResult) {
        return new DiffCommandConfiguration(parseResult);
    }
}
