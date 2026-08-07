package com.cycrilabs.keycloak.configurator.commands.diff.boundary;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;

import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class GroupDiffer extends AbstractDiffer<GroupRepresentation> {
    @Override
    public EntityType getType() {
        return EntityType.GROUP;
    }

    @Override
    protected GroupRepresentation loadEntity(final ConfigurationFile file) {
        return entityLoader.loadEntity(file.getFile(), GroupRepresentation.class);
    }

    @Override
    protected GroupRepresentation findServerEntity(final ConfigurationFile file,
            final GroupRepresentation local) {
        return findGroupByPath(file.getRealmName(), local.getPath());
    }

    /**
     * Groups are identified by their path, so that groups of the same name below different
     * parents are distinguished.
     */
    @Override
    protected String getEntityName(final ConfigurationFile file, final GroupRepresentation local) {
        return local.getPath();
    }

    /**
     * The realm roles of a group are served by a separate endpoint and are not part of the group
     * representation. They are added here so that a locally declared 'realmRoles' can be
     * compared at all.
     */
    @Override
    protected GroupRepresentation normalizeServerEntity(final GroupRepresentation server,
            final ConfigurationFile file, final GroupRepresentation local) {
        if (local.getRealmRoles() == null) {
            return server;
        }

        server.setRealmRoles(keycloak.realm(file.getRealmName())
                .groups()
                .group(server.getId())
                .roles()
                .realmLevel()
                .listAll()
                .stream()
                .map(RoleRepresentation::getName)
                .toList());
        return server;
    }

    @Override
    protected Set<String> getServerEntityNames(final String realm) {
        final Set<String> paths = new LinkedHashSet<>();
        collectPaths(realm, keycloak.realm(realm).groups().groups(), paths);
        return paths;
    }

    /**
     * Collects the paths of the given groups and of all their descendants.
     *
     * @param realm
     *         the realm the groups belong to
     * @param groups
     *         the groups to collect
     * @param paths
     *         collects the resulting paths
     */
    private void collectPaths(final String realm, final List<GroupRepresentation> groups,
            final Set<String> paths) {
        for (final GroupRepresentation group : groups) {
            paths.add(group.getPath());
            collectPaths(realm, getSubGroups(realm, group), paths);
        }
    }

    private List<GroupRepresentation> getSubGroups(final String realm,
            final GroupRepresentation group) {
        try {
            return keycloak.realm(realm)
                    .groups()
                    .group(group.getId())
                    .getSubGroups(null, null, Boolean.TRUE);
        } catch (final RuntimeException e) {
            Log.debugf("Could not read sub groups of group '%s' in realm '%s': %s", group.getPath(),
                    realm, e.getMessage());
            return List.of();
        }
    }

    private GroupRepresentation findGroupByPath(final String realm, final String path) {
        if (path == null) {
            Log.warnf("Cannot look up a group without a path in realm '%s'.", realm);
            return null;
        }

        try {
            return keycloak.realm(realm).getGroupByPath(path);
        } catch (final NotFoundException e) {
            return null;
        }
    }
}
