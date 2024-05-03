package com.cycrilabs.keycloak.configurator.commands.export.boundary;

import jakarta.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.StringUtils;
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
        if (StringUtils.isNotBlank(configuration.getClient())) {
            keycloak.realm(configuration.getRealmName())
                    .clients()
                    .findByClientId(configuration.getClient())
                    .stream()
                    .findFirst()
                    .ifPresent(client -> exportEntity(entityName, client));
        } else {
            keycloak.realm(configuration.getRealmName())
                    .clients()
                    .findAll()
                    .forEach(client -> exportEntity(entityName, client));
        }
    }

    private void exportEntity(final String entityName, final ClientRepresentation client) {
        final RoleRepresentation entity = keycloak.realm(configuration.getRealmName())
                .clients()
                .get(client.getId())
                .roles()
                .get(entityName)
                .toRepresentation();
        Log.infof("Exporting client role '%s'.", entity.getName());
        writeFile(JsonUtil.toJson(entity), entity.getName(), configuration.getRealmName(),
                client.getClientId());
    }

    @Override
    protected void exportEntities() {
        if (StringUtils.isNotBlank(configuration.getClient())) {
            keycloak.realm(configuration.getRealmName())
                    .clients()
                    .findByClientId(configuration.getClient())
                    .stream()
                    .findFirst()
                    .ifPresent(this::exportEntities);
        } else {
            keycloak.realm(configuration.getRealmName())
                    .clients()
                    .findAll()
                    .forEach(this::exportEntities);
        }
    }

    private void exportEntities(final ClientRepresentation client) {
        keycloak.realm(configuration.getRealmName())
                .clients()
                .get(client.getId())
                .roles()
                .list()
                .forEach(entity -> {
                    Log.infof("Exporting client role '%s' for client '%s'.", entity.getName(),
                            client.getClientId());
                    writeFile(JsonUtil.toJson(entity), entity.getName(),
                            configuration.getRealmName(), client.getClientId());
                });
    }
}
