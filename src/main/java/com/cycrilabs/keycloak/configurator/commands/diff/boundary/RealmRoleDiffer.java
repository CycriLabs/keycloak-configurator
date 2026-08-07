package com.cycrilabs.keycloak.configurator.commands.diff.boundary;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import org.keycloak.representations.idm.RoleRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

@ApplicationScoped
public class RealmRoleDiffer extends AbstractDiffer<RoleRepresentation> {
    @Override
    public EntityType getType() {
        return EntityType.REALM_ROLE;
    }

    @Override
    protected RoleRepresentation loadEntity(final ConfigurationFile file) {
        return entityLoader.loadEntity(file.getFile(), RoleRepresentation.class);
    }

    @Override
    protected RoleRepresentation findServerEntity(final ConfigurationFile file,
            final RoleRepresentation local) {
        return keycloakCache.getRoleByName(file.getRealmName(), local.getName());
    }

    @Override
    protected String getEntityName(final ConfigurationFile file, final RoleRepresentation local) {
        return local.getName();
    }

    @Override
    protected Set<String> getIgnoredPaths() {
        final Set<String> ignoredPaths = new HashSet<>(super.getIgnoredPaths());
        // Composite role membership is served by a separate endpoint and is not applied by the
        // 'configure' command either, so it is out of scope for the comparison.
        ignoredPaths.add("composites");
        return ignoredPaths;
    }

    @Override
    protected Set<String> getServerEntityNames(final String realm) {
        return keycloak.realm(realm)
                .roles()
                .list()
                .stream()
                .map(RoleRepresentation::getName)
                .collect(Collectors.toSet());
    }
}
