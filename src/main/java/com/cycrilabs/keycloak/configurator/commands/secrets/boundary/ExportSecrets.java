package com.cycrilabs.keycloak.configurator.commands.secrets.boundary;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.runtime.parser.ParseException;
import org.eclipse.microprofile.config.Config;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;

import com.cycrilabs.keycloak.configurator.commands.secrets.entity.ExportSecretsCommandConfiguration;
import com.cycrilabs.keycloak.configurator.shared.control.EnvironmentVariableProvider;
import com.cycrilabs.keycloak.configurator.shared.control.KeycloakFactory;
import com.cycrilabs.keycloak.configurator.shared.control.StringUtil;
import com.cycrilabs.keycloak.configurator.shared.control.VelocityUtils;

import io.quarkus.logging.Log;

@ApplicationScoped
public class ExportSecrets {
    private static final String VARIABLE_REALM = "realm";
    private static final String VARIABLE_AUTH_SERVER_URL = "auth_server_url";
    private static final String VARIABLE_CLIENT_ID = "client_id";
    private static final String VARIABLE_CLIENT = "client";
    private static final String VARIABLE_SECRET = "secret";
    private static final String VARIABLE_CLIENTS = "clients";
    private static final String VARIABLE_ENVIRONMENT = "env";

    ExportSecretsCommandConfiguration configuration;
    EnvironmentVariableProvider environmentVariableProvider;
    Keycloak keycloak;
    Config config;

    @Inject
    public ExportSecrets(final ExportSecretsCommandConfiguration configuration,
            final EnvironmentVariableProvider environmentVariableProvider,
            final Config config) {
        this.configuration = configuration;
        this.environmentVariableProvider = environmentVariableProvider;
        this.config = config;

        this.keycloak = KeycloakFactory.create(configuration);
    }

    public void export() throws IOException, ParseException {
        final Collection<Template> templates = VelocityUtils.loadTemplates(loadTemplateFiles());
        final Map<String, String> environmentVariables = environmentVariableProvider.load();
        final Map<String, ClientRepresentation> clients = loadClients();
        final Collection<ClientRepresentation> filteredClients = getFilteredClients(clients);
        for (final ClientRepresentation client : filteredClients) {
            // skip all clients without a secret
            if (StringUtil.isBlank(client.getSecret())) {
                continue;
            }

            final Collection<Template> clientTemplates = deriveClientTemplates(client, templates);
            for (final Template template : clientTemplates) {
                Log.infof("Generating secret file(s) for client '%s'.", client.getClientId());
                writeFiles(client, environmentVariables, clients, template);
            }
        }
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

    private Collection<ClientRepresentation> getFilteredClients(
            final Map<String, ClientRepresentation> clients) {
        if (StringUtil.isBlank(configuration.getClientIds())) {
            return clients.values();
        }
        final String[] split = configuration.getClientIds().split(",");
        return Arrays.stream(split)
                .map(clients::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    /**
     * Derives all templates that must be expanded for the given client.
     *
     * @param client
     *         the target client
     * @param templates
     *         all secret templates
     * @return a list of relevant target secret templates for the given client
     */
    private Collection<Template> deriveClientTemplates(final ClientRepresentation client,
            final Collection<Template> templates) {
        final Map<String, Template> clientTemplates = new HashMap<>();
        for (final Template template : templates) {
            final String name = template.getName();
            final String derivedName = getDerivedFilename(client.getClientId(), name);

            // if the template is a generic one and there is no specialized entry at the moment
            // it is added to the map
            if (isGenericSecretTemplate(name) && !clientTemplates.containsKey(derivedName)) {
                // we store the derived name temporarily to have it being overwritten by a more
                // specific template
                clientTemplates.put(derivedName, template);
            }

            // if the template contains the clientId in its name and is not generic
            // it is always added to the map
            // the generic check makes sure, there is no file that matches by accident with a client
            if (!isGenericSecretTemplate(name) && name.contains(client.getClientId())) {
                clientTemplates.put(name, template);
            }
        }
        return clientTemplates.values();
    }

    private void writeFiles(final ClientRepresentation client,
            final Map<String, String> environmentVariables,
            final Map<String, ClientRepresentation> clients, final Template template) {
        final String fileContent =
                generateFileContent(client, environmentVariables, clients, template);
        final Path targetFile = getTargetFile(client.getClientId(), template.getName());
        try {
            Files.writeString(targetFile, fileContent, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            Log.errorf("Failed to write file '%s'.", targetFile.toString());
        }
    }

    private String generateFileContent(final ClientRepresentation client,
            final Map<String, String> environmentVariables,
            final Map<String, ClientRepresentation> clients, final Template template) {
        return VelocityUtils.mergeTemplate(template, VelocityUtils.createVelocityContext(
                Map.ofEntries(
                        Map.entry(VARIABLE_REALM, configuration.getRealmName()),
                        Map.entry(VARIABLE_AUTH_SERVER_URL, configuration.getServer()),
                        Map.entry(VARIABLE_CLIENT_ID, client.getClientId()),
                        Map.entry(VARIABLE_CLIENT, client),
                        Map.entry(VARIABLE_SECRET, client.getSecret()),
                        Map.entry(VARIABLE_CLIENTS, clients),
                        Map.entry(VARIABLE_ENVIRONMENT, environmentVariables)
                )
        ));
    }

    private Path getTargetFile(final String clientId, final String templateName) {
        final String filename = getDerivedFilename(clientId, templateName);
        return Paths.get(configuration.getOutputDirectory(), filename);
    }

    private String getDerivedFilename(final String clientId, final String templateName) {
        return templateName.replace(VARIABLE_CLIENT_ID, clientId);
    }

    private boolean isGenericSecretTemplate(final String templateName) {
        return templateName.contains(VARIABLE_CLIENT_ID);
    }
}
