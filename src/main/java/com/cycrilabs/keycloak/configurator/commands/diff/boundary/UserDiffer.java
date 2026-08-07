package com.cycrilabs.keycloak.configurator.commands.diff.boundary;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class UserDiffer extends AbstractDiffer<UserRepresentation> {
    /**
     * Above this number of users, the users of a realm are not enumerated to find users without a
     * local configuration. A realm federating an LDAP directory can hold tens of thousands of
     * users, none of which are meant to be configured as code.
     */
    private static final int MAX_ENUMERATED_USERS = 200;

    @Override
    public EntityType getType() {
        return EntityType.USER;
    }

    @Override
    protected UserRepresentation loadEntity(final ConfigurationFile file) {
        return entityLoader.loadEntity(file.getFile(), UserRepresentation.class);
    }

    @Override
    protected UserRepresentation findServerEntity(final ConfigurationFile file,
            final UserRepresentation local) {
        return findUserByExactUsername(file.getRealmName(), local.getUsername());
    }

    @Override
    protected String getEntityName(final ConfigurationFile file, final UserRepresentation local) {
        return local.getUsername();
    }

    @Override
    protected Set<String> getIgnoredPaths() {
        final Set<String> ignoredPaths = new HashSet<>(super.getIgnoredPaths());
        // The server never returns credential values, so a locally configured password can not
        // be compared. Reporting it would be a difference on every single run.
        ignoredPaths.add("credentials");
        // Group membership is served by a separate endpoint and is not applied by the 'configure'
        // command either, so it is out of scope for the comparison.
        ignoredPaths.add("groups");
        return ignoredPaths;
    }

    /**
     * Role mappings are served by separate endpoints and are not part of the user representation.
     * They are added here so that locally declared roles can be compared at all.
     */
    @Override
    protected UserRepresentation normalizeServerEntity(final UserRepresentation server,
            final ConfigurationFile file, final UserRepresentation local) {
        final String realm = file.getRealmName();
        if (local.getRealmRoles() != null) {
            server.setRealmRoles(getAssignedRealmRoles(realm, server.getId()));
        }
        if (local.getClientRoles() != null) {
            server.setClientRoles(
                    getAssignedClientRoles(realm, server.getId(), local.getClientRoles().keySet()));
        }
        return server;
    }

    private List<String> getAssignedRealmRoles(final String realm, final String userId) {
        return keycloak.realm(realm)
                .users()
                .get(userId)
                .roles()
                .realmLevel()
                .listAll()
                .stream()
                .map(RoleRepresentation::getName)
                .filter(role -> !builtInEntityFilter.isAutoAssignedRole(realm, role))
                .toList();
    }

    /**
     * Reads the assigned client roles for the clients that the local configuration declares.
     * Clients that are not declared locally are not read, as they are not being configured.
     */
    private Map<String, List<String>> getAssignedClientRoles(final String realm,
            final String userId, final Set<String> declaredClientIds) {
        final Map<String, List<String>> assignedRoles = new LinkedHashMap<>();
        for (final String clientId : declaredClientIds) {
            final ClientRepresentation client = keycloakCache.getClientByClientId(realm, clientId);
            if (client == null) {
                Log.debugf("Cannot read client roles of unknown client '%s' in realm '%s'.",
                        clientId, realm);
                continue;
            }

            assignedRoles.put(clientId, keycloak.realm(realm)
                    .users()
                    .get(userId)
                    .roles()
                    .clientLevel(client.getId())
                    .listAll()
                    .stream()
                    .map(RoleRepresentation::getName)
                    .toList());
        }
        return assignedRoles;
    }

    @Override
    protected Set<String> getServerEntityNames(final String realm) {
        final Integer userCount = keycloak.realm(realm).users().count();
        if (userCount != null && userCount > MAX_ENUMERATED_USERS) {
            Log.warnf("Realm '%s' holds %d users, which is more than the limit of %d. Users "
                            + "without a local configuration are not reported for this realm.",
                    realm, userCount, Integer.valueOf(MAX_ENUMERATED_USERS));
            return Set.of();
        }

        return keycloak.realm(realm)
                .users()
                .list()
                .stream()
                .map(UserRepresentation::getUsername)
                .collect(Collectors.toSet());
    }
}
