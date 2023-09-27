package com.cycrilabs.keycloak.configurator.commands.export.boundary;

import jakarta.enterprise.context.ApplicationScoped;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import com.cycrilabs.keycloak.configurator.shared.control.JsonUtil;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class ClientRoleExporter extends AbstractExporter {
    @Override
    public EntityType getType() {
        return EntityType.CLIENT_ROLE;
    }

    @Override
    protected void exportEntity(final String entityName) {
        final ClientRepresentation clientRepresentation =
                keycloak.realm(configuration.getRealmName())
                        .clients()
                        .findByClientId(configuration.getClient())
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Client not found."));
        final RoleRepresentation entity = keycloak.realm(configuration.getRealmName())
                .clients()
                .get(clientRepresentation.getId())
                .roles()
                .get(entityName)
                .toRepresentation();

        Log.infof("Exporting client role '%s'.", entity.getName());
        writeFile(JsonUtil.toJson(entity), entity.getName(),
                configuration.getRealmName(), clientRepresentation.getClientId());
    }

    @Override
    protected void exportEntities() {
        final ClientRepresentation clientRepresentation =
                keycloak.realm(configuration.getRealmName())
                        .clients()
                        .findByClientId(configuration.getClient())
                        .stream()
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Client not found."));
        keycloak.realm(configuration.getRealmName())
                .clients()
                .get(clientRepresentation.getId())
                .roles()
                .list()
                .forEach(entity -> {
                    Log.infof("Exporting client role '%s'.", entity.getName());
                    writeFile(JsonUtil.toJson(entity), entity.getName(),
                            configuration.getRealmName(), clientRepresentation.getClientId());
                });
    }
}
