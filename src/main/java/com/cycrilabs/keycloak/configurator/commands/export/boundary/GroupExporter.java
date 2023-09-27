package com.cycrilabs.keycloak.configurator.commands.export.boundary;

import jakarta.enterprise.context.ApplicationScoped;

import com.cycrilabs.keycloak.configurator.shared.control.JsonUtil;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class GroupExporter extends AbstractExporter {
    @Override
    public EntityType getType() {
        return EntityType.GROUP;
    }

    @Override
    protected void exportEntity(final String entityName) {
        keycloak.realm(configuration.getRealmName())
                .groups()
                .groups()
                .stream()
                .filter(group -> group.getName().equals(entityName))
                .forEach(group -> {
                    Log.infof("Exporting group '%s'.", group.getName());
                    writeFile(JsonUtil.toJson(group), group.getName(),
                            configuration.getRealmName());
                });
    }

    @Override
    protected void exportEntities() {
        keycloak.realm(configuration.getRealmName())
                .groups()
                .groups()
                .forEach(group -> {
                    Log.infof("Exporting group '%s'.", group.getName());
                    writeFile(JsonUtil.toJson(group), group.getName(),
                            configuration.getRealmName());
                });
    }
}
