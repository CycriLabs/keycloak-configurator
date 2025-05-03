package com.cycrilabs.keycloak.configurator.commands.secrets.control;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.enterprise.context.ApplicationScoped;

import org.apache.velocity.Template;
import org.keycloak.representations.idm.ClientRepresentation;

import com.cycrilabs.keycloak.configurator.commands.secrets.entity.ExportSecretsCommandConfiguration;
import com.cycrilabs.keycloak.configurator.shared.control.StringUtil;
import com.cycrilabs.keycloak.configurator.shared.control.VelocityUtils;

import io.quarkus.logging.Log;

@ApplicationScoped
public class SecretFilesGenerator {
    private static final String VARIABLE_REALM = "realm";
    private static final String VARIABLE_AUTH_SERVER_URL = "auth_server_url";
    private static final String VARIABLE_CLIENT_ID = "client_id";
    private static final String VARIABLE_CLIENT = "client";
    private static final String VARIABLE_SECRET = "secret";
    private static final String VARIABLE_CLIENTS = "clients";
    private static final String VARIABLE_ENVIRONMENT = "env";

    private ExportSecretsCommandConfiguration configuration;
    private Collection<Template> templates;
    private Map<String, String> environmentVariables;
    private Map<String, ClientRepresentation> clients;

    public void generateSecretFiles(final ExportSecretsCommandConfiguration configuration,
            final Collection<Template> templates, final Map<String, String> environmentVariables,
            final Map<String, ClientRepresentation> clients,
            final SecretFileContentWriter secretFileWriter) {
        this.configuration = configuration;
        this.templates = templates;
        this.environmentVariables = environmentVariables;
        this.clients = clients;

        final Collection<ClientRepresentation> filteredClients = getFilteredClients();
        for (final ClientRepresentation client : filteredClients) {
            // skip all clients without a secret
            if (StringUtil.isBlank(client.getSecret())) {
                continue;
            }

            final Collection<Template> clientTemplates = deriveClientTemplates(client);
            for (final Template template : clientTemplates) {
                Log.infof("Generating secret file(s) for client '%s' based on '%s'.",
                        client.getClientId(), template.getName());
                final String fileContent = generateFileContent(client, template);
                final String derivedName = getDerivedFilename(client, template);
                secretFileWriter.provideContent(derivedName, fileContent);
            }
        }
    }

    private Collection<ClientRepresentation> getFilteredClients() {
        final Stream<ClientRepresentation> filteredClients =
                StringUtil.isBlank(configuration.getClientIds())
                ? clients.values().stream()
                : Arrays.stream(configuration.getClientIds().split(","))
                        .map(clients::get)
                        .filter(Objects::nonNull)
                        .collect(Collectors.toSet())
                        .stream();
        // clients are sorted by clientId to have a deterministic order;
        // clients with short names are sorted first to have them in the top of the list and
        // have the generated files being overwritten by more specific ones
        return filteredClients
                .sorted(Comparator.comparingInt(
                                (ClientRepresentation c) -> c.getClientId().length())
                        .thenComparing(ClientRepresentation::getClientId))
                .toList();
    }

    /**
     * Derives all templates that must be expanded for the given client.
     *
     * @param client
     *         the target client
     * @return a list of relevant target secret templates for the given client
     */
    private Collection<Template> deriveClientTemplates(final ClientRepresentation client) {
        final Map<String, Template> clientTemplates = new HashMap<>();
        for (final Template template : templates) {
            final String name = template.getName();
            final String derivedName = getDerivedFilename(client, template);

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

    private String getDerivedFilename(final ClientRepresentation client, final Template template) {
        return template.getName().replace(VARIABLE_CLIENT_ID, client.getClientId());
    }

    private boolean isGenericSecretTemplate(final String templateName) {
        return templateName.contains(VARIABLE_CLIENT_ID);
    }

    private String generateFileContent(final ClientRepresentation client, final Template template) {
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
}
