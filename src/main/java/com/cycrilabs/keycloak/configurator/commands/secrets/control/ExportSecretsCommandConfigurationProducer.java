package com.cycrilabs.keycloak.configurator.commands.secrets.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import com.cycrilabs.keycloak.configurator.commands.secrets.entity.ExportSecretsCommandConfiguration;

import picocli.CommandLine;

@ApplicationScoped
public class ExportSecretsCommandConfigurationProducer {
    @Produces
    @ApplicationScoped
    ExportSecretsCommandConfiguration createConfiguration(
            final CommandLine.ParseResult parseResult) {
        return new ExportSecretsCommandConfiguration(parseResult);
    }
}
