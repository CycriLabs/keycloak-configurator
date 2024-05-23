package com.cycrilabs.keycloak.configurator.commands.export.boundary;

import jakarta.enterprise.context.ApplicationScoped;

import org.keycloak.representations.idm.ComponentRepresentation;

import com.cycrilabs.keycloak.configurator.shared.control.JsonUtil;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class ComponentExporter extends AbstractExporter {
    @Override
    public EntityType getType() {
        return EntityType.COMPONENT;
    }

    @Override
    protected void exportEntity(final String entityName) {
        final ComponentRepresentation component = keycloak.realm(configuration.getRealmName())
                .components()
                .component(entityName)
                .toRepresentation();
        Log.infof("Exporting component '%s'.", component.getName());
        writeFile(JsonUtil.toJson(component), component.getName(), configuration.getRealmName());
    }

    @Override
    protected void exportEntities() {
        keycloak.realm(configuration.getRealmName())
                .components()
                .query()
                .forEach(component -> {
                    Log.infof("Exporting component '%s'.", component.getName());
                    writeFile(JsonUtil.toJson(component), component.getName(),
                            configuration.getRealmName());
                });
    }
}
