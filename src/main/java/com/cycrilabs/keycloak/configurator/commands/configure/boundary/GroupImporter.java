package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import static jakarta.ws.rs.core.Response.Status.Family.familyOf;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ImporterStatus;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class GroupImporter extends AbstractImporter<GroupRepresentation> {
    @Override
    public EntityType getType() {
        return EntityType.GROUP;
    }

    @Override
    protected GroupRepresentation loadEntity(final ConfigurationFile file) {
        final GroupRepresentation entity = loadEntity(file, GroupRepresentation.class);
        if (configuration.isDryRun()) {
            Log.infof("Loaded group '%s' from file '%s'.", entity.getName(), file.getFile());
        }
        return entity;
    }

    @Override
    protected GroupRepresentation executeImport(final ConfigurationFile file,
            final GroupRepresentation group) {
        final String realmName = file.getRealmName();

        final GroupRepresentation existingGroup = findGroupByPath(realmName, group.getPath());
        if (existingGroup != null) {
            Log.infof("Group '%s' already exists in realm '%s'. Skipping import.", group.getName(),
                    realmName);
            return existingGroup;
        }

        // Documentation suggests, that child groups could be created the same way as root level groups.
        // (See org.keycloak.admin.client.resource.GroupsResource#add)
        // Alas, this does not seem to work. So we have to make a distinction here
        final GroupRepresentation parentGroup = findParentGroup(realmName, group.getPath());
        if (parentGroup == null) {
            createGroupAtRoot(realmName, group);
        } else {
            createChildGroup(realmName, parentGroup, group);
        }

        final GroupRepresentation importedGroup = findGroupByPath(realmName, group.getPath());
        if (importedGroup == null) {
            Log.errorf("Could not import group '%s' for realm '%s'", group.getName(), realmName);
            return null;
        }
        Log.infof("Loaded imported group '%s' from realm '%s'.", importedGroup.getName(),
                realmName);

        // add realm roles to group: they are not added automatically when a new group is created
        addRealmRoles(realmName, importedGroup, group.getRealmRoles());

        return importedGroup;
    }

    private void createGroupAtRoot(final String realmName, final GroupRepresentation group) {
        try (final Response response = keycloak.realm(realmName)
                .groups()
                .add(group)) {
            if (isConflict(response)) {
                Log.infof("Could not import group for realm '%s': %s", realmName,
                        extractError(response).getErrorMessage());
            } else {
                Log.infof("Group '%s' imported for realm '%s'.", group.getName(), realmName);
            }
        } catch (final ClientErrorException e) {
            setStatus(ImporterStatus.FAILURE);
            Log.errorf("Could not import group for realm '%s': %s", realmName, e.getMessage());
        }
    }

    private void createChildGroup(final String realmName, final GroupRepresentation parentGroup,
            final GroupRepresentation group) {
        final Response response = keycloak.realm(realmName)
                .groups()
                .group(parentGroup.getId()).subGroup(group);

        if (familyOf(response.getStatus()) != Response.Status.Family.SUCCESSFUL) {
            Log.errorf("Could not import group for realm '%s': %s", realmName,
                    extractError(response).getErrorMessage());
        }
    }

    private GroupRepresentation findGroupByPath(final String realmName, final String path) {
        try {
            return keycloak.realm(realmName).getGroupByPath(path);
        } catch (final NotFoundException e) {
            return null;
        }
    }

    private GroupRepresentation findParentGroup(final String realmName, final String path) {
        final String optParentPath = getParentPath(path);
        if (optParentPath == null) {
            return null;
        }
        return findGroupByPath(realmName, optParentPath);
    }

    private String getParentPath(final String path) {
        final List<String> components = Arrays.stream(path.split("/"))
                .filter(s -> !s.isEmpty())
                .toList();
        if (components.size() <= 1) {
            return null;
        }
        return components.subList(0, components.size() - 1)
                .stream()
                .collect(Collectors.joining("/", "/", ""));
    }

    private void addRealmRoles(final String realmName, final GroupRepresentation importedGroup,
            final List<String> realmRoles) {
        if (realmRoles == null || realmRoles.isEmpty()) {
            return;
        }

        Log.infof("Adding realm roles to group '%s' in realm '%s'.", importedGroup.getName(),
                realmName);
        final List<RoleRepresentation> roleRepresentations = realmRoles
                .stream()
                .map(roleName -> keycloakCache.getRoleByName(realmName, roleName))
                .toList();
        keycloak.realm(realmName)
                .groups()
                .group(importedGroup.getId())
                .roles()
                .realmLevel()
                .add(roleRepresentations);
    }
}
