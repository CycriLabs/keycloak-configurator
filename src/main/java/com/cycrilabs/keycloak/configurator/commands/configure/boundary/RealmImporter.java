package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import java.nio.file.Path;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;

import org.keycloak.representations.idm.RealmRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.EntityImportType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class RealmImporter extends AbstractImporter {
    @Override
    public EntityImportType getType() {
        return EntityImportType.REALM;
    }

    @Override
    protected RealmRepresentation importFile(final Path file) {
        final RealmRepresentation realm = loadEntity(file, RealmRepresentation.class);

        try {
            keycloak.realms()
                    .create(realm);
            Log.infof("Realm '%s' imported.", realm.getRealm());
        } catch (final ClientErrorException e) {
            Log.errorf("Could not import realm from file: %s", e.getMessage());
        }

        final RealmRepresentation importedRealm = keycloak.realms()
                .realm(realm.getRealm())
                .toRepresentation();
        Log.infof("Loaded realm '%s' from server.", importedRealm.getRealm());
        entityStore.addRealm(importedRealm);
        return importedRealm;
    }
}
