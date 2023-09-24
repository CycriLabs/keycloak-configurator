package com.cycrilabs.keycloak.configurator.commands.secrets.control;

import jakarta.inject.Inject;

import com.cycrilabs.keycloak.configurator.commands.secrets.boundary.ExportSecrets;
import com.cycrilabs.keycloak.configurator.commands.secrets.entity.ExportSecretsCommandConfiguration;
import com.cycrilabs.keycloak.configurator.shared.control.KeycloakOptions;

import io.quarkus.logging.Log;
import picocli.CommandLine;

@CommandLine.Command(name = "export-secrets", mixinStandardHelpOptions = true)
public class ExportSecretsCommand implements Runnable {
    @CommandLine.Mixin
    KeycloakOptions keycloakOptions;
    @CommandLine.Option(required = true, names = { "-r", "--realm" },
            description = "Realm name to export secrets from.")
    String realm;
    @CommandLine.Option(names = { "-c", "--config" },
            description = "Directory containing templates for secret output files.")
    String configDirectory;
    @CommandLine.Option(names = { "-o", "--output" }, defaultValue = "./",
            description = "Output directory for generate files.")
    String outputDirectory;

    @Inject
    ExportSecretsCommandConfiguration configuration;
    @Inject
    ExportSecrets secretExporter;

    @Override
    public void run() {
        try {
            Log.infof("Exporting secrets from realm '%s'.", configuration.getRealmName());
            secretExporter.export();
        } catch (final Exception e) {
            Log.errorf(e, "Failed to export secrets from realm '%s'.",
                    configuration.getRealmName());
        }
    }
}
