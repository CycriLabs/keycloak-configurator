package com.cycrilabs.eam.keycloak.configurator.commands.configure.control;

import java.util.HashMap;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import lombok.Getter;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;

@ApplicationScoped
public class EntityStore {
    @Getter
    public static class EntityStoreEntry<E, C> {
        private final E entity;
        private final Map<String, C> children = new HashMap<>();

        public EntityStoreEntry(final E entity) {
            this.entity = entity;
        }

        public void addChild(final String key, final C child) {
            children.put(key, child);
        }
    }

    private final Map<String, EntityStoreEntry<RealmRepresentation, ClientRepresentation>> realms =
            new HashMap<>();

    public void addRealm(final RealmRepresentation realm) {
        realms.put(realm.getRealm(), new EntityStoreEntry<>(realm));
    }

    public void addClient(final String realmName, final ClientRepresentation importedClient) {
        realms.get(realmName)
                .addChild(importedClient.getClientId(), importedClient);
    }

    public ClientRepresentation getClient(final String realmName, final String clientId) {
        return realms.get(realmName)
                .getChildren()
                .get(clientId);
    }
}
