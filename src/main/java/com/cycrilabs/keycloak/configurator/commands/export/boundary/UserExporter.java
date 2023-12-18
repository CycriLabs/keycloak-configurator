package com.cycrilabs.keycloak.configurator.commands.export.boundary;

import jakarta.enterprise.context.ApplicationScoped;

import com.cycrilabs.keycloak.configurator.shared.control.JsonUtil;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class UserExporter extends AbstractExporter {
    @Override
    public EntityType getType() {
        return EntityType.USER;
    }

    @Override
    protected void exportEntity(final String entityName) {
        keycloak.realm(configuration.getRealmName())
                .users()
                .search(entityName)
                .forEach(user -> {
                    Log.infof("Exporting user '%s'.", user.getUsername());
                    writeFile(JsonUtil.toJson(user), user.getUsername(),
                            configuration.getRealmName());
                });
    }

    @Override
    protected void exportEntities() {
        keycloak.realm(configuration.getRealmName())
                .users()
                .list()
                .forEach(user -> {
                    Log.infof("Exporting user '%s'.", user.getUsername());
                    writeFile(JsonUtil.toJson(user), user.getUsername(),
                            configuration.getRealmName());
                });
    }
}
