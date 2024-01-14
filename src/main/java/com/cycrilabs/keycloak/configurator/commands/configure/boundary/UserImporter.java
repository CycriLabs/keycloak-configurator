package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import java.nio.file.Path;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import com.cycrilabs.keycloak.configurator.shared.control.JsonUtil;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class UserImporter extends AbstractImporter {
    @Override
    public EntityType getType() {
        return EntityType.USER;
    }

    @Override
    protected Object importFile(final Path file) {
        final UserRepresentation user = JsonUtil.loadEntity(file, UserRepresentation.class);

        final String[] fileNameParts = file.toString().split(PATH_SEPARATOR);
        final String realmName = fileNameParts[fileNameParts.length - 3];

        try (final Response response = keycloak.realm(realmName)
                .users()
                .create(user)) {
            if (response.getStatus() == 409) {
                Log.errorf("Could not import user from file for realm '%s': %s", realmName,
                        response.readEntity(ErrorRepresentation.class)
                                .getErrorMessage());
            } else {
                Log.infof("User '%s' imported for realm '%s'.", user.getEmail(), realmName);
            }
        } catch (final ClientErrorException e) {
            Log.errorf("Could not import user from file for realm '%s': %s", realmName,
                    e.getMessage());
        }

        return loadImportedUser(realmName, user);
    }

    private UserRepresentation loadImportedUser(final String realmName,
            final UserRepresentation user) {
        final List<UserRepresentation> searchResult = keycloak.realm(realmName)
                .users()
                .search(user.getUsername());
        if (searchResult.isEmpty()) {
            Log.infof("Could not load imported user '%s' from realm '%s'.", user.getUsername(),
                    realmName);
            return null;
        }
        final UserRepresentation importedUser = searchResult.getFirst();
        Log.infof("Loaded imported user '%s' from realm '%s'.", importedUser.getEmail(), realmName);
        return importedUser;
    }
}
