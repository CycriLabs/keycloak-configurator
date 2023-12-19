package com.cycrilabs.keycloak.configurator.commands.secrets.boundary;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.runtime.parser.ParseException;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;

import com.cycrilabs.keycloak.configurator.commands.secrets.entity.ExportSecretsCommandConfiguration;
import com.cycrilabs.keycloak.configurator.shared.control.KeycloakFactory;
import com.cycrilabs.keycloak.configurator.shared.control.VelocityUtils;

import io.quarkus.logging.Log;

@ApplicationScoped
public class ExportSecrets {
    private static final String VARIABLE_CLIENT_ID = "client_id";
    private static final String VARIABLE_REALM = "realm";
    private static final String VARIABLE_AUTH_SERVER_URL = "auth_server_url";
    private static final String VARIABLE_SECRET = "secret";

    @Inject
    ExportSecretsCommandConfiguration configuration;
    Keycloak keycloak;

    @PostConstruct
    public void init() {
        keycloak = KeycloakFactory.create(configuration);
    }

    public void export() throws IOException, ParseException, URISyntaxException {
        final Collection<Template> templates = VelocityUtils.loadTemplates(loadTemplateFiles());
        final List<ClientRepresentation> clients = loadClientSecrets();
        for (final ClientRepresentation client : clients) {
            for (final Template template : templates) {
                Log.infof("Generating secret file(s) for client '%s'.", client.getClientId());
                writeFiles(client, template);
            }
        }
    }

    private Collection<File> loadTemplateFiles() {
        return FileUtils.listFiles(new File(configuration.getConfigDirectory()), null, true);
    }

    private List<ClientRepresentation> loadClientSecrets() {
        return keycloak.realm(configuration.getRealmName())
                .clients()
                .findAll()
                .stream()
                .filter(client -> client.getSecret() != null)
                .toList();
    }

    private void writeFiles(final ClientRepresentation client, final Template template) {
        final String fileContent = generateFileContent(client, template);
        final Path targetFile = getTargetFile(client.getClientId(), template.getName());
        try {
            Files.writeString(targetFile, fileContent, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            Log.errorf("Failed to write file '%s'.", targetFile.toString());
        }
    }

    private String generateFileContent(final ClientRepresentation client, final Template template) {
        return VelocityUtils.mergeTemplate(template, VelocityUtils.createVelocityContext(
                Map.ofEntries(
                        Map.entry(VARIABLE_REALM, configuration.getRealmName()),
                        Map.entry(VARIABLE_AUTH_SERVER_URL, configuration.getServer()),
                        Map.entry(VARIABLE_SECRET, client.getSecret()),
                        Map.entry(VARIABLE_CLIENT_ID, client.getClientId())
                )
        ));
    }

    private Path getTargetFile(final String clientId, final String templateName) {
        final String filename = templateName.replace(VARIABLE_CLIENT_ID, clientId);
        return Paths.get(configuration.getOutputDirectory(), filename);
    }
}
