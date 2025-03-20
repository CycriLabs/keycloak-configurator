package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import java.nio.file.Path;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;

import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ImporterStatus;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class RealmRoleImporter extends AbstractImporter<RoleRepresentation> {
    @Override
    public EntityType getType() {
        return EntityType.REALM_ROLE;
    }

    @Override
    protected RoleRepresentation loadEntity(final Path file) {
        final RoleRepresentation entity = loadEntity(file, RoleRepresentation.class);
        if (configuration.isDryRun()) {
            Log.infof("Loaded realm role '%s' from file '%s'.", entity.getName(), file);
        }
        return entity;
    }

    @Override
    protected RoleRepresentation executeImport(final Path file, final RoleRepresentation role) {
        final String[] fileNameParts = file.toString().split(PATH_SEPARATOR);
        final String realmName = fileNameParts[fileNameParts.length - 3];

        try {
            keycloak.realm(realmName)
                    .roles()
                    .create(role);
            Log.infof("Realm role '%s' imported for realm '%s'.", role.getName(), realmName);
        } catch (final ClientErrorException e) {
            if (isConflict(e.getResponse())) {
                Log.infof("Could not import '%s' realm role for realm '%s': %s", role.getName(),
                        realmName, extractError(e).getErrorMessage());
            } else {
                setStatus(ImporterStatus.FAILURE);
                final ErrorRepresentation error = extractError(e);
                final String message = error != null
                                       ? error.getErrorMessage()
                                       : e.getMessage();
                Log.errorf("Could not import '%s' realm role for realm '%s': %s", role.getName(),
                        realmName, message);
            }
        }

        try {
            final RoleRepresentation importedRole = keycloak.realm(realmName)
                    .roles()
                    .get(role.getName())
                    .toRepresentation();
            Log.infof("Loaded imported realm role '%s' from realm '%s'.", importedRole.getName(),
                    realmName);
            return importedRole;
        } catch (final ClientErrorException e) {
            Log.errorf("Could not load imported realm role '%s' from realm '%s': %s",
                    role.getName(),
                    realmName, e.getMessage());
            return null;
        }
    }
}
