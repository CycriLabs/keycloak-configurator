package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ErrorRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

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
        final UserRepresentation user = loadEntity(file, UserRepresentation.class);

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
            return null;
        }

        // we need to load the imported user to get the id
        final UserRepresentation userRepresentation =
                fetchUserByUsername(realmName, user.getUsername());
        if (userRepresentation != null) {
            addRealmRoles(realmName, userRepresentation, user.getRealmRoles());
            addClientRoles(realmName, userRepresentation, user.getClientRoles());

            // we need to load the imported user again to get the updated version with roles
            return fetchUserByUsername(realmName, user.getUsername());
        }

        return null;
    }

    private UserRepresentation fetchUserByUsername(final String realmName, final String username) {
        final List<UserRepresentation> searchResult = keycloak.realm(realmName)
                .users()
                .search(username);
        if (searchResult.isEmpty()) {
            Log.infof("Could not load imported user '%s' from realm '%s'.", username, realmName);
            return null;
        }
        final UserRepresentation user = searchResult.getFirst();
        Log.infof("Loaded imported user '%s' from realm '%s'.", user.getUsername(), realmName);
        return user;
    }

    private void addRealmRoles(final String realmName, final UserRepresentation user,
            final List<String> realmRoles) {
        Log.infof("Adding realm roles '%s' to user '%s' in realm '%s'.", realmRoles,
                user.getUsername(), realmName);

        final Map<String, RoleRepresentation> availableRealmRoles = keycloak.realm(realmName)
                .roles()
                .list()
                .stream()
                .collect(Collectors.toMap(RoleRepresentation::getName, Function.identity()));
        keycloak.realm(realmName)
                .users()
                .get(user.getId())
                .roles()
                .realmLevel()
                .add(realmRoles.stream()
                        .filter(availableRealmRoles::containsKey)
                        .map(availableRealmRoles::get)
                        .toList());
    }

    private void addClientRoles(final String realmName, final UserRepresentation user,
            final Map<String, List<String>> clientRoles) {
        clientRoles.forEach((clientName, roles) -> {
            Log.infof("Adding client roles '%s' to user '%s' for client '%s' in realm '%s'.",
                    roles, user.getUsername(), clientName, realmName);

            final ClientRepresentation client = entityStore.getClient(realmName, clientName);
            final Map<String, RoleRepresentation> availableClientRoles = keycloak.realm(realmName)
                    .clients()
                    .get(client.getId())
                    .roles()
                    .list()
                    .stream()
                    .collect(Collectors.toMap(RoleRepresentation::getName, Function.identity()));
            keycloak.realm(realmName)
                    .users()
                    .get(user.getId())
                    .roles()
                    .clientLevel(client.getId())
                    .add(clientRoles.get(clientName)
                            .stream()
                            .filter(availableClientRoles::containsKey)
                            .map(availableClientRoles::get)
                            .toList());
        });
    }
}
