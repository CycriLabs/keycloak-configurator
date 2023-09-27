package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import java.nio.file.Path;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ErrorRepresentation;

import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class ClientImporter extends AbstractImporter {
    @Override
    public EntityType getType() {
        return EntityType.CLIENT;
    }

    @Override
    protected ClientRepresentation importFile(final Path file) {
        final ClientRepresentation client = loadEntity(file, ClientRepresentation.class);

        final String[] fileNameParts = file.toString().split(PATH_SEPARATOR);
        final String realmName = fileNameParts[fileNameParts.length - 2];

        try (final Response response = keycloak.realm(realmName)
                .clients()
                .create(client)) {
            if (response.getStatus() == 409) {
                Log.errorf("Could not import client from file: %s",
                        response.readEntity(ErrorRepresentation.class)
                                .getErrorMessage());
            } else {
                Log.infof("Client '%s' imported.", client.getClientId());
            }
        } catch (final ClientErrorException e) {
            Log.errorf("Could not import client from file: %s", e.getMessage());
        }

        final ClientRepresentation importedClient = keycloak.realm(realmName)
                .clients()
                .findByClientId(client.getClientId())
                .get(0);
        Log.infof("Loaded client role '%s' from server.", importedClient.getClientId());
        entityStore.addClient(realmName, importedClient);
        return importedClient;
    }
}
