package com.cycrilabs.keycloak.configurator.commands.configure.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigureCommandConfiguration;

import picocli.CommandLine;

@ApplicationScoped
public class ConfigureCommandConfigurationProducer {
    @Produces
    @ApplicationScoped
    ConfigureCommandConfiguration createConfiguration(final CommandLine.ParseResult parseResult) {
        return new ConfigureCommandConfiguration(
                parseResult.subcommand().matchedOption("c").getValue().toString()
        );
    }
}
