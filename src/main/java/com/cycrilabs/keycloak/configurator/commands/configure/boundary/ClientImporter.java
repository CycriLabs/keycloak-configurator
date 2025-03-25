package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.ClientRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ImporterStatus;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class ClientImporter extends AbstractImporter<ClientRepresentation> {
    @Override
    public EntityType getType() {
        return EntityType.CLIENT;
    }

    @Override
    protected ClientRepresentation loadEntity(final ConfigurationFile file) {
        final ClientRepresentation entity = loadEntity(file, ClientRepresentation.class);
        if (configuration.isDryRun()) {
            Log.infof("Loaded client '%s' from file '%s'.", entity.getClientId(), file.getFile());
        }
        return entity;
    }

    @Override
    protected ClientRepresentation executeImport(final ConfigurationFile file,
            final ClientRepresentation client) {
        final String realmName = file.getRealmName();

        try (final Response response = keycloak.realm(realmName)
                .clients()
                .create(client)) {
            if (isConflict(response)) {
                Log.infof("Could not import client for realm '%s': %s", realmName,
                        extractError(response).getErrorMessage());
            } else {
                Log.infof("Client '%s' imported for realm '%s'.", client.getClientId(), realmName);
            }
        } catch (final ClientErrorException e) {
            setStatus(ImporterStatus.FAILURE);
            Log.errorf("Could not import client for realm '%s': %s", realmName, e.getMessage());
        }

        final ClientRepresentation importedClient =
                keycloakCache.getClientByClientId(realmName, client.getClientId());
        Log.infof("Loaded client '%s' from realm '%s'.", importedClient.getClientId(),
                realmName);
        return importedClient;
    }
}
