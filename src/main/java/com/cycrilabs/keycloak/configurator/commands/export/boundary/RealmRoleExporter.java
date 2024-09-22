package com.cycrilabs.keycloak.configurator.commands.export.boundary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.control.EntityStore;
import com.cycrilabs.keycloak.configurator.shared.control.JsonUtil;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class RealmRoleExporter extends AbstractExporter {
    EntityStore entityStore;

    @Inject
    public RealmRoleExporter(final EntityStore entityStore) {
        this.entityStore = entityStore;
    }

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
                .stream()
                .map(this::createRealmRoleWithComposites)
                .forEach(entity -> {
                    Log.infof("Exporting realm role '%s'.", entity.getName());
                    writeFile(JsonUtil.toJson(entity), entity.getName(),
                            configuration.getRealmName());
                });
    }

    private RoleRepresentation createRealmRoleWithComposites(final RoleRepresentation entity) {
        final Set<RoleRepresentation> compositeRoles = keycloak.realm(configuration.getRealmName())
                .rolesById()
                .getRoleComposites(entity.getId());

        final RoleRepresentation.Composites composites = new RoleRepresentation.Composites();
        composites.setRealm(new HashSet<>());
        composites.setClient(new HashMap<>());

        for (final RoleRepresentation compositeRole : compositeRoles) {
            Log.info(compositeRole.getDescription());
            final ClientRepresentation client = loadClient(compositeRole.getContainerId());
            composites.getClient().computeIfAbsent(client.getClientId(), k -> new ArrayList<>())
                    .add(compositeRole.getName());
        }

        entity.setComposites(composites);
        return entity;
    }

    private ClientRepresentation loadClient(final String id) {
        final ClientRepresentation client = entityStore.getClient(configuration.getRealmName(), id);
        if (client != null) {
            return client;
        }

        final ClientRepresentation loadedClient = keycloak.realm(configuration.getRealmName())
                .clients()
                .get(id)
                .toRepresentation();
        entityStore.addClient(configuration.getRealmName(), loadedClient);
        return loadedClient;
    }
}
