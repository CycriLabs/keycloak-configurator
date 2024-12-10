package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import java.nio.file.Path;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ImporterStatus;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class ClientRoleImporter extends AbstractImporter {
    @Override
    public EntityType getType() {
        return EntityType.CLIENT_ROLE;
    }

    @Override
    protected RoleRepresentation importFile(final Path file) {
        final RoleRepresentation role = loadEntity(file, RoleRepresentation.class);

        final String[] fileNameParts = file.toString().split(PATH_SEPARATOR);
        final String realmName = fileNameParts[fileNameParts.length - 4];
        final String clientId = fileNameParts[fileNameParts.length - 2];
        final ClientRepresentation client = entityStore.getClient(realmName, clientId);

        try {
            keycloak.realm(realmName)
                    .clients()
                    .get(client.getId())
                    .roles()
                    .create(role);
            Log.infof("Client role '%s' imported for client '%s' of realm '%s'.", role.getName(),
                    clientId, realmName);
        } catch (final ClientErrorException e) {
            if (isConflict(e.getResponse())) {
                Log.infof("Could not import client role for client '%s' of realm '%s': %s",
                        clientId, realmName, extractError(e).getErrorMessage());
            } else {
                setStatus(ImporterStatus.FAILURE);
                Log.errorf("Could not import client role for client '%s' of realm '%s': %s",
                        clientId, realmName, e.getMessage());
            }
        }

        return keycloak.realm(realmName)
                .clients()
                .get(client.getId())
                .roles()
                .get(role.getName())
                .toRepresentation();
    }
}
