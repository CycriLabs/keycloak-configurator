package com.cycrilabs.keycloak.configurator.commands.diff.boundary;

import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;

import org.keycloak.representations.idm.RealmRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

@ApplicationScoped
public class RealmDiffer extends AbstractDiffer<RealmRepresentation> {
    /**
     * Collections that the realm endpoint does not serve. They are either part of a full realm
     * export only, or they are reported by the differ of their own entity type. Comparing them
     * against the realm representation would report every entity as missing on the server.
     */
    private static final Set<String> COLLECTIONS_NOT_SERVED_BY_REALM_ENDPOINT = Set.of(
            "clients",
            "clientScopes",
            "clientTemplates",
            "roles",
            "groups",
            "users",
            "federatedUsers",
            "components",
            "applications",
            "oauthClients",
            "authenticationFlows",
            "authenticatorConfig",
            "requiredActions",
            "scopeMappings",
            "clientScopeMappings",
            "protocolMappers",
            "identityProviders",
            "identityProviderMappers",
            "clientProfiles",
            "clientPolicies");

    @Override
    public EntityType getType() {
        return EntityType.REALM;
    }

    @Override
    protected RealmRepresentation loadEntity(final ConfigurationFile file) {
        return entityLoader.loadEntity(file.getFile(), RealmRepresentation.class);
    }

    @Override
    protected RealmRepresentation findServerEntity(final ConfigurationFile file,
            final RealmRepresentation local) {
        return keycloakCache.getRealmByName(local.getRealm());
    }

    /**
     * The realm of a realm entity is the realm itself. It is read from the file content, because
     * realm files carry no realm name when read from a nested directory structure.
     */
    @Override
    protected String getRealmName(final ConfigurationFile file, final RealmRepresentation local) {
        return local.getRealm();
    }

    @Override
    protected String getEntityName(final ConfigurationFile file, final RealmRepresentation local) {
        return local.getRealm();
    }

    @Override
    protected Set<String> getIgnoredPaths() {
        final Set<String> ignoredPaths = new HashSet<>(super.getIgnoredPaths());
        ignoredPaths.addAll(COLLECTIONS_NOT_SERVED_BY_REALM_ENDPOINT);
        return ignoredPaths;
    }

    /**
     * Realms on the server without a local configuration are reported as a single finding. The
     * differs of the other entity types only descend into locally configured realms, so an
     * unrelated realm does not produce findings for each of its entities.
     */
    @Override
    protected void reportExtras() {
        keycloak.realms()
                .findAll()
                .stream()
                .map(RealmRepresentation::getRealm)
                .filter(realm -> !configuredRealms.contains(realm))
                .forEach(realm -> reportExtra(realm, realm));
    }
}
