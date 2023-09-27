package com.cycrilabs.keycloak.configurator.commands.export.boundary;

import jakarta.enterprise.context.ApplicationScoped;

import org.keycloak.representations.idm.RealmRepresentation;

import com.cycrilabs.keycloak.configurator.shared.control.JsonUtil;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class RealmExporter extends AbstractExporter {
    @Override
    public EntityType getType() {
        return EntityType.REALM;
    }

    @Override
    protected void exportEntity(final String entityName) {
        final RealmRepresentation entity = keycloak.realm(entityName).toRepresentation();
        Log.infof("Exporting realm '%s'.", entity.getRealm());
        writeFile(JsonUtil.toJson(entity), entity.getRealm(), "");
    }

    @Override
    protected void exportEntities() {
        keycloak.realms()
                .findAll()
                .forEach(entity -> {
                    Log.infof("Exporting realm '%s'.", entity.getRealm());
                    writeFile(JsonUtil.toJson(entity), entity.getRealm(), "");
                });
    }
}
