package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import java.nio.file.Path;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;

import org.keycloak.representations.idm.RoleRepresentation;

import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class RealmRoleImporter extends AbstractImporter {
    @Override
    public EntityType getType() {
        return EntityType.REALM_ROLE;
    }

    @Override
    protected Object importFile(final Path file) {
        final RoleRepresentation role = loadEntity(file, RoleRepresentation.class);

        final String[] fileNameParts = file.toString().split(PATH_SEPARATOR);
        final String realmName = fileNameParts[fileNameParts.length - 2];

        try {
            keycloak.realm(realmName)
                    .roles()
                    .create(role);
            Log.infof("Realm role '%s' imported.", role.getName());
        } catch (final ClientErrorException e) {
            Log.errorf("Could not import realm role from file: %s", e.getMessage());
        }

        final RoleRepresentation importedRole = keycloak.realm(realmName)
                .roles()
                .get(role.getName())
                .toRepresentation();
        Log.infof("Loaded imported realm role '%s' from server.", importedRole.getName());
        return importedRole;
    }
}
