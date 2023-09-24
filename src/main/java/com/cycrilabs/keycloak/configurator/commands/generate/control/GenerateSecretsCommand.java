package com.cycrilabs.keycloak.configurator.commands.generate.control;

import jakarta.inject.Inject;

import com.cycrilabs.keycloak.configurator.commands.generate.boundary.GenerateSecrets;
import com.cycrilabs.keycloak.configurator.commands.generate.entity.GenerateSecretsCommandConfiguration;
import com.cycrilabs.keycloak.configurator.shared.control.KeycloakOptions;

import io.quarkus.logging.Log;
import picocli.CommandLine;

@CommandLine.Command(name = "rotate-secrets", mixinStandardHelpOptions = true)
public class GenerateSecretsCommand implements Runnable {
    @CommandLine.Mixin
    KeycloakOptions keycloakOptions;
    @CommandLine.Option(required = true, names = { "-r", "--realm" },
            description = "Realm name to generate secrets for.")
    String realm;
    @CommandLine.Option(names = { "-c", "--client" },
            description = "Specific client to generate new secret.")
    String clientId;

    @Inject
    GenerateSecretsCommandConfiguration configuration;
    @Inject
    GenerateSecrets command;

    @Override
    public void run() {
        try {
            Log.infof("Generating secrets of realm '%s'.", configuration.getRealmName());
            command.run();
        } catch (final Exception e) {
            Log.errorf(e, "Failed to generate secrets of realm '%s'.",
                    configuration.getRealmName());
        }
    }
}
