package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import java.nio.file.Path;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;

import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import com.cycrilabs.keycloak.configurator.shared.control.JsonUtil;
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
        final RoleRepresentation role = JsonUtil.loadEntity(file, RoleRepresentation.class);

        final String[] fileNameParts = file.toString().split(PATH_SEPARATOR);
        final String realmName = fileNameParts[fileNameParts.length - 3];

        try {
            keycloak.realm(realmName)
                    .roles()
                    .create(role);
            Log.infof("Realm role '%s' imported for realm '%s'.", role.getName(), realmName);
        } catch (final ClientErrorException e) {
            Log.errorf("Could not import realm role for realm '%s': %s", realmName, e.getMessage());
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
            Log.errorf("Could not load imported realm role '%s' from realm '%s': %s", role.getName(),
                    realmName, e.getMessage());
            return null;
        }
    }
}
