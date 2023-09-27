package com.cycrilabs.keycloak.configurator.commands.export.boundary;

import jakarta.enterprise.context.ApplicationScoped;

import com.cycrilabs.keycloak.configurator.shared.control.JsonUtil;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class ClientExporter extends AbstractExporter {
    @Override
    public EntityType getType() {
        return EntityType.CLIENT;
    }

    @Override
    protected void exportEntity(final String entityName) {
        keycloak.realm(configuration.getRealmName())
                .clients()
                .findAll()
                .stream()
                .filter(client -> client.getClientId().equals(entityName))
                .forEach(client -> {
                    Log.infof("Exporting client '%s'.", client.getClientId());
                    writeFile(JsonUtil.toJson(client), client.getClientId(),
                            configuration.getRealmName());
                });
    }

    @Override
    protected void exportEntities() {
        keycloak.realm(configuration.getRealmName())
                .clients()
                .findAll()
                .forEach(client -> {
                    Log.infof("Exporting client '%s'.", client.getClientId());
                    writeFile(JsonUtil.toJson(client), client.getClientId(),
                            configuration.getRealmName());
                });
    }
}
