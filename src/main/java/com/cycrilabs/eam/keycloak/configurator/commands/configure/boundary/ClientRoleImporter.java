package com.cycrilabs.eam.keycloak.configurator.commands.configure.boundary;

import java.nio.file.Path;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import com.cycrilabs.eam.keycloak.configurator.commands.configure.entity.EntityImportType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class ClientRoleImporter extends AbstractImporter {
    @Override
    public EntityImportType getType() {
        return EntityImportType.CLIENT_ROLE;
    }

    @Override
    protected RoleRepresentation importFile(final Path file) {
        final RoleRepresentation role = loadEntity(file, RoleRepresentation.class);

        final String[] fileNameParts = file.toString().split(PATH_SEPARATOR);
        final String realmName = fileNameParts[fileNameParts.length - 3];
        final String clientId = fileNameParts[fileNameParts.length - 2];
        final ClientRepresentation client = entityStore.getClient(realmName, clientId);

        try {
            keycloak.realm(realmName)
                    .clients()
                    .get(client.getId())
                    .roles()
                    .create(role);
            Log.infof("Client role '%s' imported.", role.getName());
        } catch (final ClientErrorException e) {
            Log.errorf("Could not import client role from file: %s", e.getMessage());
        }

        return keycloak.realm(realmName)
                .clients()
                .get(client.getId())
                .roles()
                .get(role.getName())
                .toRepresentation();
    }
}
