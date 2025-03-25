package com.cycrilabs.keycloak.configurator.shared.boundary;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class KeycloakCache {
    @Inject
    Keycloak keycloak;

    // Cache structure: Map<EntityType, Map<RealmName, Map<EntityId, Entity>>>
    private final Map<EntityType, Map<String, Map<String, Object>>> cache =
            new ConcurrentHashMap<>();

    public KeycloakCache() {
        Log.infof("Initializing Keycloak cache.");
    }

    public RealmRepresentation getRealmByName(final String realmName) {
        final Map<String, Map<String, Object>> realmCache =
                cache.computeIfAbsent(EntityType.REALM, k -> new ConcurrentHashMap<>());
        final Map<String, Object> singleRealmCache =
                realmCache.computeIfAbsent("all", k -> new ConcurrentHashMap<>());

        RealmRepresentation realm = (RealmRepresentation) singleRealmCache.get(realmName);

        if (realm == null) {
            Log.debugf("Realm '%s' not found in cache, fetching from Keycloak.", realmName);
            try {
                realm = keycloak.realms()
                        .realm(realmName)
                        .toRepresentation();
                singleRealmCache.put(realmName, realm);
                Log.debugf("Cached realm '%s'.", realmName);
            } catch (final Exception e) {
                Log.debugf("Could not find realm '%s': %s", realmName, e.getMessage());
            }
        } else {
            Log.debugf("Retrieved realm '%s' from cache.", realmName);
        }

        return realm;
    }

    public ClientRepresentation getClientByClientId(final String realmName, final String clientId) {
        final Map<String, Map<String, Object>> realmCache =
                cache.computeIfAbsent(EntityType.CLIENT, k -> new ConcurrentHashMap<>());
        final Map<String, Object> clientCache =
                realmCache.computeIfAbsent(realmName, k -> new ConcurrentHashMap<>());

        ClientRepresentation client = (ClientRepresentation) clientCache.get(clientId);

        if (client == null) {
            Log.debugf("Client '%s' not found in cache for realm '%s', fetching from Keycloak.",
                    clientId, realmName);
            final List<ClientRepresentation> clients = keycloak.realm(realmName)
                    .clients()
                    .findByClientId(clientId);
            if (!clients.isEmpty()) {
                client = clients.getFirst();
                clientCache.put(clientId, client);
                Log.debugf("Cached client '%s' for realm '%s'.", clientId, realmName);
            }
        } else {
            Log.debugf("Retrieved client '%s' for realm '%s' from cache.", clientId, realmName);
        }

        return client;
    }

    public RoleRepresentation getRoleByName(final String realmName, final String roleName) {
        final Map<String, Map<String, Object>> realmCache =
                cache.computeIfAbsent(EntityType.REALM_ROLE, k -> new ConcurrentHashMap<>());
        final Map<String, Object> roleCache =
                realmCache.computeIfAbsent(realmName, k -> new ConcurrentHashMap<>());

        RoleRepresentation role = (RoleRepresentation) roleCache.get(roleName);

        if (role == null) {
            Log.debugf("Role '%s' not found in cache for realm '%s', fetching from Keycloak.",
                    roleName, realmName);
            try {
                role = keycloak.realm(realmName)
                        .roles()
                        .get(roleName)
                        .toRepresentation();
                roleCache.put(roleName, role);
                Log.debugf("Cached role '%s' for realm '%s'.", roleName, realmName);
            } catch (final Exception e) {
                Log.debugf("Could not find role '%s' in realm '%s': %s", roleName, realmName,
                        e.getMessage());
            }
        } else {
            Log.debugf("Retrieved role '%s' for realm '%s' from cache.", roleName, realmName);
        }

        return role;
    }

    public RoleRepresentation getClientRoleByName(final String realmName, final String clientId,
            final String roleName) {
        // First get the client representation
        final ClientRepresentation client = getClientByClientId(realmName, clientId);

        if (client == null) {
            Log.debugf("Could not find client '%s' in realm '%s'.", clientId, realmName);
            return null;
        }

        // Create or get the cache for CLIENT_ROLE
        final Map<String, Map<String, Object>> realmCache =
                cache.computeIfAbsent(EntityType.CLIENT_ROLE, k -> new ConcurrentHashMap<>());

        // Use composite key for client roles (realm:clientId)
        final String cacheKey = realmName + ":" + clientId;
        final Map<String, Object> roleCache =
                realmCache.computeIfAbsent(cacheKey, k -> new ConcurrentHashMap<>());

        // Check if role is in cache
        RoleRepresentation role = (RoleRepresentation) roleCache.get(roleName);

        if (role == null) {
            Log.debugf(
                    "Client role '%s' not found in cache for client '%s' in realm '%s', fetching from Keycloak.",
                    roleName, clientId, realmName);
            try {
                role = keycloak.realm(realmName)
                        .clients()
                        .get(client.getId())
                        .roles()
                        .get(roleName)
                        .toRepresentation();

                // Cache the result
                roleCache.put(roleName, role);
                Log.debugf("Cached client role '%s' for client '%s' in realm '%s'.", roleName,
                        clientId, realmName);
            } catch (final Exception e) {
                Log.debugf("Could not find client role '%s' for client '%s' in realm '%s': %s",
                        roleName, clientId, realmName, e.getMessage());
            }
        } else {
            Log.debugf("Retrieved client role '%s' for client '%s' in realm '%s' from cache.",
                    roleName, clientId, realmName);
        }

        return role;
    }

    public UserRepresentation getUserByUsername(final String realmName, final String username) {
        final Map<String, Map<String, Object>> realmCache =
                cache.computeIfAbsent(EntityType.USER, k -> new ConcurrentHashMap<>());
        final Map<String, Object> userCache =
                realmCache.computeIfAbsent(realmName, k -> new ConcurrentHashMap<>());

        UserRepresentation user = (UserRepresentation) userCache.get(username);

        if (user == null) {
            Log.debugf("User '%s' not found in cache for realm '%s', fetching from Keycloak.",
                    username, realmName);
            final List<UserRepresentation> users = keycloak.realm(realmName)
                    .users()
                    .search(username);
            if (!users.isEmpty()) {
                user = users.getFirst();
                userCache.put(username, user);
                Log.debugf("Cached user '%s' for realm '%s'.", username, realmName);
            }
        } else {
            Log.debugf("Retrieved user '%s' for realm '%s' from cache.", username, realmName);
        }

        return user;
    }
}
