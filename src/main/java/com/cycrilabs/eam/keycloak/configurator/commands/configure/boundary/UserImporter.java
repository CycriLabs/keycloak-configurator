package com.cycrilabs.eam.keycloak.configurator.commands.configure.boundary;

import java.nio.file.Path;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import com.cycrilabs.eam.keycloak.configurator.commands.configure.entity.EntityImportType;
import com.cycrilabs.eam.keycloak.configurator.shared.control.JsonbConfigurator;

import io.quarkus.logging.Log;

@ApplicationScoped
public class UserImporter extends AbstractImporter {
    @Override
    public EntityImportType getType() {
        return EntityImportType.USER;
    }

    @Override
    protected Object importFile(final Path file) {
        final UserRepresentation user = loadEntity(file, UserRepresentation.class);

        final String[] fileNameParts = file.toString().split(PATH_SEPARATOR);
        final String realmName = fileNameParts[fileNameParts.length - 2];

        try (final Response response = keycloak.realm(realmName)
                .users()
                .create(user)) {
            if (response.getStatus() == 409) {
                Log.errorf("Could not import user from file: %s",
                        response.readEntity(ErrorRepresentation.class)
                                .getErrorMessage());
            } else {
                Log.infof("User '%s' imported.", user.getEmail());
            }
        } catch (final ClientErrorException e) {
            Log.errorf("Could not import user from file: %s", e.getMessage());
        }

        final UserRepresentation importedUser = keycloak.realm(realmName)
                .users()
                .search("alice@maildrop.cc")
                .get(0);
        Log.infof("Loaded imported user '%s' from server.", importedUser.getEmail());
        return importedUser;
    }
}
