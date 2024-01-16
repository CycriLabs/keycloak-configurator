package com.cycrilabs.keycloak.configurator.commands.configure.control;

import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

import lombok.Getter;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;

import io.quarkus.logging.Log;

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

    @PostConstruct
    public void init() {
        Log.infof("Initializing entity store.");
    }

    public void addRealm(final RealmRepresentation realm) {
        Log.debugf("Adding realm '%s' to entity store.", realm.getRealm());
        realms.put(realm.getRealm(), new EntityStoreEntry<>(realm));
    }

    public void addClient(final String realmName, final ClientRepresentation importedClient) {
        Log.debugf("Adding client '%s' to realm '%s' in entity store.", importedClient.getClientId(),
                realmName);
        realms.get(realmName)
                .addChild(importedClient.getClientId(), importedClient);
    }

    public ClientRepresentation getClient(final String realmName, final String clientId) {
        return realms.get(realmName) != null
               ? realms.get(realmName)
                       .getChildren()
                       .get(clientId)
               : null;
    }
}
