package com.cycrilabs.keycloak.configurator.commands.generate.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import com.cycrilabs.keycloak.configurator.commands.generate.entity.GenerateSecretsCommandConfiguration;

import picocli.CommandLine;

@ApplicationScoped
public class GenerateSecretsCommandConfigurationProducer {
    @Produces
    @ApplicationScoped
    GenerateSecretsCommandConfiguration createConfiguration(
            final CommandLine.ParseResult parseResult) {
        return new GenerateSecretsCommandConfiguration(parseResult);
    }
}
