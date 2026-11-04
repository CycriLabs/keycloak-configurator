package com.cycrilabs.keycloak.configurator.commands.export.boundary;

import jakarta.enterprise.context.ApplicationScoped;

import org.keycloak.admin.client.resource.GroupsResource;

import com.cycrilabs.keycloak.configurator.shared.control.JsonUtil;
import com.cycrilabs.keycloak.configurator.shared.control.PagingUtil;
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
        final GroupsResource groups = keycloak.realm(configuration.getRealmName()).groups();
        PagingUtil.listAll(groups::groups)
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
        final GroupsResource groups = keycloak.realm(configuration.getRealmName()).groups();
        PagingUtil.listAll(groups::groups)
                .forEach(group -> {
                    Log.infof("Exporting group '%s'.", group.getName());
                    writeFile(JsonUtil.toJson(group), group.getName(),
                            configuration.getRealmName());
                });
    }
}
