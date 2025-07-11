package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import static jakarta.ws.rs.core.Response.Status.Family.familyOf;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.GroupRepresentation;

import com.cycrilabs.keycloak.configurator.shared.control.JsonUtil;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class GroupImporter extends AbstractImporter {
    @Override
    public EntityType getType() {
        return EntityType.GROUP;
    }

    @Override
    protected Object importFile(final Path file) {
        final GroupRepresentation group = JsonUtil.loadEntity(file, GroupRepresentation.class);

        final String[] fileNameParts = file.toString().split(PATH_SEPARATOR);
        final String realmName = fileNameParts[fileNameParts.length - 3];

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

        return importedGroup;
    }

    private void createGroupAtRoot(final String realmName, final GroupRepresentation group) {
        try (final Response response = keycloak.realm(realmName)
                .groups()
                .add(group)) {
            if (response.getStatus() == 409) {
                Log.infof("Could not import group for realm '%s': %s", realmName,
                        extractError(response).getErrorMessage());
            } else {
                Log.infof("Group '%s' imported for realm '%s'.", group.getName(), realmName);
            }
        } catch (final ClientErrorException e) {
            Log.errorf("Could not import group for realm '%s': %s", realmName, e.getMessage());
        }
    }

    private void createChildGroup(final String realmName, final GroupRepresentation parentGroup,
            final GroupRepresentation group) {
        final Response response = keycloak.realm(realmName)
                .groups()
                .group(parentGroup.getId())
                .subGroup(group);

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
}
