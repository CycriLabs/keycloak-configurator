package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ServiceUserRealmRoleMappingDTO;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class ServiceAccountRealmRoleImporter extends AbstractImporter {
    @Override
    public EntityType getType() {
        return EntityType.SERVICE_ACCOUNT_REALM_ROLE;
    }

    @Override
    protected Object importFile(final Path file) {
        final String[] fileNameParts = file.toString().split(PATH_SEPARATOR);
        final String realmName = fileNameParts[fileNameParts.length - 4];
        final String serviceUsername = fileNameParts[fileNameParts.length - 2];

        Log.debugf(
                "Importing service account realm roles '%s' for service user '%s' of realm '%s'.",
                file.getFileName(), serviceUsername, realmName);

        final UserRepresentation user = loadUserByUsername(realmName, serviceUsername);
        if (user == null) {
            return null;
        }

        Log.debugf("Found service user '%s' of realm '%s'.", user.getUsername(), realmName);

        importServiceUserRealmRoleMappings(file, realmName, user);

        return null;
    }

    private UserRepresentation loadUserByUsername(final String realmName, final String username) {
        try {
            final List<UserRepresentation> userRepresentations = keycloak.realm(realmName)
                    .users()
                    .searchByUsername(username, Boolean.TRUE);
            if (userRepresentations.size() == 1) {
                return userRepresentations.getFirst();
            }

            Log.warnf("Found %d users '%s' of realm '%s'. Skipping import.",
                    Integer.valueOf(userRepresentations.size()), username, realmName);
        } catch (final Exception e) {
            Log.errorf("Could not find user '%s' of realm '%s': %s", username, realmName,
                    e.getMessage());
        }
        return null;
    }

    private void importServiceUserRealmRoleMappings(final Path file, final String realmName,
            final UserRepresentation serviceUser) {
        final ServiceUserRealmRoleMappingDTO serviceUserRealmRoleMappings =
                loadEntity(file, ServiceUserRealmRoleMappingDTO.class);
        final List<String> roles = serviceUserRealmRoleMappings.getRoles();

        Log.debugf("Importing realm roles '%s' for service user '%s' of realm '%s'.",
                roles.toString(), serviceUser.getUsername(), realmName);
        final Map<String, RoleRepresentation> availableRealmRoles = keycloak.realm(realmName)
                .roles()
                .list()
                .stream()
                .collect(Collectors.toMap(RoleRepresentation::getName, Function.identity()));
        Log.debugf("Found %d roles of realm '%s'.", Integer.valueOf(availableRealmRoles.size()),
                realmName);

        keycloak.realm(realmName)
                .users()
                .get(serviceUser.getId())
                .roles()
                .realmLevel()
                .add(roles.stream()
                        .filter(availableRealmRoles::containsKey)
                        .map(availableRealmRoles::get)
                        .toList());
    }
}
