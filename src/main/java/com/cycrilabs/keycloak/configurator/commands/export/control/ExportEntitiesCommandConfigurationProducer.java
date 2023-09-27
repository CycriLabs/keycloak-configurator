package com.cycrilabs.keycloak.configurator.commands.export.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import com.cycrilabs.keycloak.configurator.commands.export.entity.ExportEntitiesCommandConfiguration;

import picocli.CommandLine;

@ApplicationScoped
public class ExportEntitiesCommandConfigurationProducer {
    @Produces
    @ApplicationScoped
    ExportEntitiesCommandConfiguration createConfiguration(
            final CommandLine.ParseResult parseResult) {
        return new ExportEntitiesCommandConfiguration(parseResult);
    }
}
