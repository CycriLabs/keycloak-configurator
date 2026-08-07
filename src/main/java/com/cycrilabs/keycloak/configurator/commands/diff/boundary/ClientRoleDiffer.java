package com.cycrilabs.keycloak.configurator.commands.diff.boundary;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class ClientRoleDiffer extends AbstractDiffer<RoleRepresentation> {
    private static final String CLIENT_ROLE_SEPARATOR = ":";

    @Override
    public EntityType getType() {
        return EntityType.CLIENT_ROLE;
    }

    @Override
    protected RoleRepresentation loadEntity(final ConfigurationFile file) {
        return entityLoader.loadEntity(file.getFile(), RoleRepresentation.class);
    }

    @Override
    protected RoleRepresentation findServerEntity(final ConfigurationFile file,
            final RoleRepresentation local) {
        return keycloakCache.getClientRoleByName(file.getRealmName(), file.getClientId(),
                local.getName());
    }

    /**
     * A client role is only unique within its client, so the owning client is part of the name.
     */
    @Override
    protected String getEntityName(final ConfigurationFile file, final RoleRepresentation local) {
        return file.getClientId() + CLIENT_ROLE_SEPARATOR + local.getName();
    }

    @Override
    protected Set<String> getIgnoredPaths() {
        final Set<String> ignoredPaths = new HashSet<>(super.getIgnoredPaths());
        // Composite role membership is served by a separate endpoint and is not applied by the
        // 'configure' command either, so it is out of scope for the comparison.
        ignoredPaths.add("composites");
        return ignoredPaths;
    }

    /**
     * Roles are only enumerated for clients that have locally configured roles. Without this
     * restriction every role of every client of the realm would be reported, which says nothing
     * about a configuration that does not manage that client's roles at all.
     */
    @Override
    protected Set<String> getServerEntityNames(final String realm) {
        final Set<String> serverRoles = new LinkedHashSet<>();
        for (final String clientId : getConfiguredClientIds(realm)) {
            final ClientRepresentation client = keycloakCache.getClientByClientId(realm, clientId);
            if (client == null) {
                Log.debugf("Skipping client roles of unknown client '%s' in realm '%s'.", clientId,
                        realm);
                continue;
            }

            keycloak.realm(realm)
                    .clients()
                    .get(client.getId())
                    .roles()
                    .list()
                    .forEach(role -> serverRoles.add(
                            clientId + CLIENT_ROLE_SEPARATOR + role.getName()));
        }
        return serverRoles;
    }

    /**
     * The clients that have at least one locally configured role, derived from the names of the
     * compared entities.
     *
     * @param realm
     *         the realm to look up
     * @return the client ids with locally configured roles
     */
    private Set<String> getConfiguredClientIds(final String realm) {
        return getLocalEntityNames(realm).stream()
                .map(entity -> entity.substring(0, entity.indexOf(CLIENT_ROLE_SEPARATOR)))
                .collect(Collectors.toSet());
    }
}
