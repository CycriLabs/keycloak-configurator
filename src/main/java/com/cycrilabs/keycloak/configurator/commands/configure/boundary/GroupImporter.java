package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import java.nio.file.Path;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;

import com.cycrilabs.keycloak.configurator.shared.control.JsonUtil;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class GroupImporter extends AbstractImporter {
    @Override
    public EntityType getType() {
        return EntityType.GROUP;
    }

    @Override
    protected Object importFile(final Path file) {
        final GroupRepresentation group = JsonUtil.loadEntity(file, GroupRepresentation.class);

        final String[] fileNameParts = file.toString().split(PATH_SEPARATOR);
        final String realmName = fileNameParts[fileNameParts.length - 3];

        try (final Response response = keycloak.realm(realmName)
                .groups()
                .add(group)) {
            if (response.getStatus() == 409) {
                Log.errorf("Could not import group for realm '%s': %s", realmName,
                        response.readEntity(ErrorRepresentation.class)
                                .getErrorMessage());
            } else {
                Log.infof("Group '%s' imported for realm '%s'.", group.getName(), realmName);
            }
        } catch (final ClientErrorException e) {
            Log.errorf("Could not import group for realm '%s': %s", realmName, e.getMessage());
        }

        final GroupRepresentation importedGroup = keycloak.realm(realmName)
                .groups()
                .query(group.getName())
                .get(0);
        Log.infof("Loaded imported group '%s' from realm '%s'.", importedGroup.getName(),
                realmName);
        return importedGroup;
    }
}
