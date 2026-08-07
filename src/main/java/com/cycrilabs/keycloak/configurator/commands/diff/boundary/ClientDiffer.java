package com.cycrilabs.keycloak.configurator.commands.diff.boundary;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ClientScopeRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

@ApplicationScoped
public class ClientDiffer extends AbstractDiffer<ClientRepresentation> {
    @Override
    public EntityType getType() {
        return EntityType.CLIENT;
    }

    @Override
    protected ClientRepresentation loadEntity(final ConfigurationFile file) {
        return entityLoader.loadEntity(file.getFile(), ClientRepresentation.class);
    }

    @Override
    protected ClientRepresentation findServerEntity(final ConfigurationFile file,
            final ClientRepresentation local) {
        return keycloakCache.getClientByClientId(file.getRealmName(), local.getClientId());
    }

    @Override
    protected String getEntityName(final ConfigurationFile file,
            final ClientRepresentation local) {
        return local.getClientId();
    }

    @Override
    protected Set<String> getIgnoredPaths() {
        final Set<String> ignoredPaths = new HashSet<>(super.getIgnoredPaths());
        // Issued by the server when the client is created.
        ignoredPaths.add("registrationAccessToken");
        // Stamped by the server when the client secret is created.
        ignoredPaths.add("attributes.client.secret.creation.time");
        return ignoredPaths;
    }

    /**
     * The assigned client scopes are served by separate endpoints and are not part of the client
     * representation, although they can be declared when a client is created. They are added here
     * so that locally declared scopes can be compared instead of being reported as not set on the
     * server.
     */
    @Override
    protected ClientRepresentation normalizeServerEntity(final ClientRepresentation server,
            final ConfigurationFile file, final ClientRepresentation local) {
        if (local.getDefaultClientScopes() == null && local.getOptionalClientScopes() == null) {
            return server;
        }

        final String realm = file.getRealmName();
        final ClientResource clientResource =
                keycloak.realm(realm).clients().get(server.getId());
        if (local.getDefaultClientScopes() != null) {
            server.setDefaultClientScopes(assignedScopes(realm,
                    clientResource.getDefaultClientScopes(), local.getDefaultClientScopes()));
        }
        if (local.getOptionalClientScopes() != null) {
            server.setOptionalClientScopes(assignedScopes(realm,
                    clientResource.getOptionalClientScopes(), local.getOptionalClientScopes()));
        }
        return server;
    }

    /**
     * The scopes assigned on the server, without the built-in scopes that Keycloak assigns on its
     * own. Enabling service accounts for instance adds the 'service_account' scope, which would
     * otherwise be reported for every client. Built-in scopes that the configuration does declare
     * stay in the comparison.
     *
     * @param realm
     *         the realm of the client
     * @param assignedScopes
     *         the scopes assigned on the server
     * @param declaredScopes
     *         the scopes declared by the local configuration
     * @return the scope names to compare against
     */
    private List<String> assignedScopes(final String realm,
            final List<ClientScopeRepresentation> assignedScopes,
            final List<String> declaredScopes) {
        return assignedScopes.stream()
                .map(ClientScopeRepresentation::getName)
                .filter(scope -> declaredScopes.contains(scope)
                        || !builtInEntityFilter.isIgnored(EntityType.CLIENT_SCOPE, realm, scope))
                .toList();
    }

    @Override
    protected Set<String> getServerEntityNames(final String realm) {
        return keycloak.realm(realm)
                .clients()
                .findAll()
                .stream()
                .map(ClientRepresentation::getClientId)
                .collect(Collectors.toSet());
    }
}
