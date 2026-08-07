package com.cycrilabs.keycloak.configurator.commands.diff.boundary;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Inject;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.UserRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.control.ConfigurationFileStore;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.commands.diff.control.BuiltInEntityFilter;
import com.cycrilabs.keycloak.configurator.commands.diff.control.ConfiguredRealms;
import com.cycrilabs.keycloak.configurator.commands.diff.control.DiffReporter;
import com.cycrilabs.keycloak.configurator.commands.diff.control.JsonDiffEngine;
import com.cycrilabs.keycloak.configurator.commands.diff.entity.DiffCommandConfiguration;
import com.cycrilabs.keycloak.configurator.commands.diff.entity.Difference;
import com.cycrilabs.keycloak.configurator.shared.boundary.KeycloakCache;
import com.cycrilabs.keycloak.configurator.shared.control.ConfigurationEntityLoader;
import com.cycrilabs.keycloak.configurator.shared.control.JsonUtil;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

/**
 * Compares all locally configured entities of one entity type against the server.
 * <p>
 * Differs are strictly read-only: they never create, update or delete anything on the server.
 *
 * @param <T>
 *         the Keycloak representation type of the compared entity
 */
public abstract class AbstractDiffer<T> {
    /**
     * Identifiers that Keycloak generates. They never match a local configuration, and a
     * configuration file exported from another server carries the identifiers of that server.
     * Entities are matched by their name, so the identifiers carry no information for a
     * comparison. This applies at any nesting depth, e.g. to protocol mappers as well.
     */
    private static final Set<String> GENERATED_IDENTIFIERS = Set.of("id", "containerId");

    /**
     * Fields that the server computes and that are never part of a configuration. 'access' holds
     * the permissions the querying admin has on the entity, which says nothing about the entity
     * itself.
     */
    private static final Set<String> SERVER_COMPUTED_FIELDS = Set.of("access");

    @Inject
    protected DiffCommandConfiguration configuration;
    @Inject
    protected ConfigurationFileStore configurationFileStore;
    @Inject
    protected ConfigurationEntityLoader entityLoader;
    @Inject
    protected KeycloakCache keycloakCache;
    @Inject
    protected Keycloak keycloak;
    @Inject
    protected DiffReporter reporter;
    @Inject
    protected BuiltInEntityFilter builtInEntityFilter;
    @Inject
    protected ConfiguredRealms configuredRealms;

    /**
     * The names of the locally configured entities per realm, collected while comparing them.
     * Used to detect entities that only exist on the server.
     */
    private final Map<String, Set<String>> localEntityNames = new HashMap<>();

    public void runDiff() {
        if (configuration.getEntityType() != null && configuration.getEntityType() != getType()) {
            Log.debugf("Skipping differ '%s' for entity type '%s'.", getClass().getSimpleName(),
                    configuration.getEntityType());
            return;
        }

        Log.infof("Executing differ '%s'.", getClass().getSimpleName());
        for (final ConfigurationFile file : configurationFileStore.getImportFiles(getType())) {
            try {
                diffEntity(file);
            } catch (final Exception e) {
                Log.errorf("Could not compare file '%s' in differ '%s': %s", file.getFile(),
                        getClass().getSimpleName(), e.getMessage());
            }
        }

        try {
            reportExtras();
        } catch (final Exception e) {
            Log.errorf("Could not determine entities missing locally in differ '%s': %s",
                    getClass().getSimpleName(), e.getMessage());
        }
    }

    /**
     * Compares a single locally configured entity against its counterpart on the server.
     *
     * @param file
     *         the configuration file of the entity
     */
    protected void diffEntity(final ConfigurationFile file) {
        final T local = loadEntity(file);
        final String realm = getRealmName(file, local);
        final String entity = getEntityName(file, local);
        rememberLocalEntity(realm, entity);
        reporter.entityChecked();

        // Without its realm an entity cannot exist either. Looking it up anyway would only
        // produce a failing request per entity, while the realm is already reported as missing.
        final T server = isRealmMissing(realm)
                         ? null
                         : findServerEntity(file, local);
        if (server == null) {
            reporter.add(Difference.missingOnServer(getType(), realm, entity));
            return;
        }

        compare(realm, entity, local, normalizeServerEntity(server, file, local));
    }

    /**
     * Compares the locally declared fields of an entity against the server and reports every
     * difference.
     *
     * @param realm
     *         the realm the entity belongs to
     * @param entity
     *         the identifying name of the entity
     * @param local
     *         the locally configured entity
     * @param server
     *         the entity as returned by the server
     */
    protected void compare(final String realm, final String entity, final Object local,
            final Object server) {
        JsonDiffEngine.compare(JsonUtil.toJsonNode(local), JsonUtil.toJsonNode(server),
                getIgnoredPaths(),
                field -> reporter.add(
                        Difference.fieldDifferent(getType(), realm, entity, field)));
    }

    /**
     * Reports all entities of this type that exist on the server but have no local configuration.
     * Built-in and auto-generated entities are filtered out.
     */
    protected void reportExtras() {
        for (final String realm : configuredRealms.get()) {
            if (isRealmMissing(realm)) {
                Log.debugf("Realm '%s' does not exist on the server, so it holds no entities.",
                        realm);
                continue;
            }

            final Set<String> configuredEntities =
                    localEntityNames.getOrDefault(realm, Set.of());
            for (final String serverEntity : getServerEntityNames(realm)) {
                if (!configuredEntities.contains(serverEntity)) {
                    reportExtra(realm, serverEntity);
                }
            }
        }
    }

    /**
     * Reports a single entity that exists on the server but is not configured locally, unless it
     * is a built-in entity.
     *
     * @param realm
     *         the realm the entity belongs to
     * @param entity
     *         the identifying name of the entity
     */
    protected void reportExtra(final String realm, final String entity) {
        if (builtInEntityFilter.isIgnored(getType(), realm, entity)) {
            Log.debugf("Ignoring built-in %s '%s' of realm '%s'.", getType().getName(), entity,
                    realm);
            return;
        }
        reporter.add(Difference.extraOnServer(getType(), realm, entity));
    }

    /**
     * The names of all entities of this type that exist in the given realm on the server.
     * Differs that cannot enumerate their entities return an empty set.
     *
     * @param realm
     *         the realm to enumerate
     * @return the identifying names of all entities on the server
     */
    protected Set<String> getServerEntityNames(final String realm) {
        return Set.of();
    }

    /**
     * Adapts the server entity so that it can be compared to the local one, e.g. by resolving
     * generated identifiers or by adding state that is served by a separate endpoint.
     *
     * @param server
     *         the entity as returned by the server
     * @param file
     *         the configuration file of the entity
     * @param local
     *         the locally configured entity
     * @return the entity to compare against
     */
    protected T normalizeServerEntity(final T server, final ConfigurationFile file, final T local) {
        return server;
    }

    /**
     * Field paths that must not be compared because both sides cannot be compared meaningfully.
     * An entry matches either the full path of a field or its name at any depth.
     *
     * @return the ignored field paths
     */
    protected Set<String> getIgnoredPaths() {
        final Set<String> ignoredPaths = new HashSet<>(GENERATED_IDENTIFIERS);
        ignoredPaths.addAll(SERVER_COMPUTED_FIELDS);
        return ignoredPaths;
    }

    /**
     * The realm of the entity. Taken from the configuration file by default.
     *
     * @param file
     *         the configuration file of the entity
     * @param local
     *         the locally configured entity
     * @return the realm name
     */
    protected String getRealmName(final ConfigurationFile file, final T local) {
        return file.getRealmName();
    }

    /**
     * Whether the given realm does not exist on the server.
     *
     * @param realm
     *         the realm to check
     * @return true if the realm is not present on the server
     */
    private boolean isRealmMissing(final String realm) {
        return realm != null && keycloakCache.getRealmByName(realm) == null;
    }

    private void rememberLocalEntity(final String realm, final String entity) {
        localEntityNames.computeIfAbsent(realm, key -> new HashSet<>()).add(entity);
    }

    /**
     * The names of all locally configured entities of this type in the given realm.
     *
     * @param realm
     *         the realm to look up
     * @return the locally configured entity names
     */
    protected Set<String> getLocalEntityNames(final String realm) {
        return localEntityNames.getOrDefault(realm, Set.of());
    }

    /**
     * Looks up a user by an exact username match, as the importers do. The fuzzy search of the
     * Keycloak cache matches substrings of several fields and could return a different user.
     *
     * @param realm
     *         the realm of the user
     * @param username
     *         the username to look up
     * @return the user, or null if no unique user was found
     */
    protected UserRepresentation findUserByExactUsername(final String realm,
            final String username) {
        final List<UserRepresentation> users =
                keycloak.realm(realm).users().searchByUsername(username, Boolean.TRUE);
        if (users.size() == 1) {
            return users.getFirst();
        }

        if (!users.isEmpty()) {
            Log.warnf("Found %d users '%s' in realm '%s'. Skipping comparison.",
                    Integer.valueOf(users.size()), username, realm);
        }
        return null;
    }

    public int getPriority() {
        return getType().getPriority();
    }

    public abstract EntityType getType();

    /**
     * Loads the entity from its configuration file.
     *
     * @param file
     *         the configuration file of the entity
     * @return the locally configured entity
     */
    protected abstract T loadEntity(final ConfigurationFile file);

    /**
     * Looks up the entity on the server.
     *
     * @param file
     *         the configuration file of the entity
     * @param local
     *         the locally configured entity
     * @return the entity on the server, or null if it does not exist
     */
    protected abstract T findServerEntity(final ConfigurationFile file, final T local);

    /**
     * The identifying name of the entity as it is shown in the report.
     *
     * @param file
     *         the configuration file of the entity
     * @param local
     *         the locally configured entity
     * @return the identifying name
     */
    protected abstract String getEntityName(final ConfigurationFile file, final T local);
}
