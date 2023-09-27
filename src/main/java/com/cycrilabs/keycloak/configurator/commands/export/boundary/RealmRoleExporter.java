package com.cycrilabs.keycloak.configurator.commands.export.boundary;

import jakarta.enterprise.context.ApplicationScoped;

import org.keycloak.representations.idm.RoleRepresentation;

import com.cycrilabs.keycloak.configurator.shared.control.JsonUtil;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class RealmRoleExporter extends AbstractExporter {
    @Override
    public EntityType getType() {
        return EntityType.REALM_ROLE;
    }

    @Override
    protected void exportEntity(final String entityName) {
        final RoleRepresentation entity = keycloak.realm(configuration.getRealmName())
                .roles()
                .get(entityName)
                .toRepresentation();
        Log.infof("Exporting realm role '%s'.", entity.getName());
        writeFile(JsonUtil.toJson(entity), entity.getName(), configuration.getRealmName());
    }

    @Override
    protected void exportEntities() {
        keycloak.realm(configuration.getRealmName())
                .roles()
                .list()
                .forEach(entity -> {
                    Log.infof("Exporting realm role '%s'.", entity.getName());
                    writeFile(JsonUtil.toJson(entity), entity.getName(),
                            configuration.getRealmName());
                });
    }
}
