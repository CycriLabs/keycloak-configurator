package com.cycrilabs.keycloak.configurator.commands.secrets.boundary;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import lombok.RequiredArgsConstructor;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.runtime.parser.ParseException;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;

import com.cycrilabs.keycloak.configurator.commands.secrets.control.SecretFilesGenerator;
import com.cycrilabs.keycloak.configurator.commands.secrets.entity.ExportSecretsCommandConfiguration;
import com.cycrilabs.keycloak.configurator.shared.control.EnvironmentVariableProvider;
import com.cycrilabs.keycloak.configurator.shared.control.VelocityUtils;

import io.quarkus.logging.Log;

@RequiredArgsConstructor
@ApplicationScoped
public class ExportSecrets {
    private final ExportSecretsCommandConfiguration configuration;
    private final EnvironmentVariableProvider environmentVariableProvider;
    private final Keycloak keycloak;
    private final SecretFilesGenerator generator;

    public void export() throws IOException, ParseException {
        final Collection<Template> templates = VelocityUtils.loadTemplates(loadTemplateFiles());
        final Map<String, String> environmentVariables = environmentVariableProvider.load();
        final Map<String, ClientRepresentation> clients = loadClients();

        generator.generateSecretFiles(configuration, templates, environmentVariables, clients,
                (final String filename, final String fileContent) -> writeFiles(fileContent,
                        getTargetPath(filename)));
    }

    private Collection<File> loadTemplateFiles() {
        return FileUtils.listFiles(new File(configuration.getConfigDirectory()), null, true);
    }

    private Map<String, ClientRepresentation> loadClients() {
        return keycloak.realm(configuration.getRealmName())
                .clients()
                .findAll()
                .stream()
                .collect(Collectors.toMap(ClientRepresentation::getClientId, Function.identity()));
    }

    private Path getTargetPath(final String filename) {
        return Paths.get(configuration.getOutputDirectory(), filename);
    }

    private void writeFiles(final String fileContent, final Path targetFile) {
        try {
            Files.writeString(targetFile, fileContent, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            Log.errorf("Failed to write file '%s'.", targetFile.toString());
        }
    }
}
