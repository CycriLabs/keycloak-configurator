package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import java.nio.file.Path;
import java.util.Optional;

import io.quarkus.runtime.util.StringUtil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;
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

        try (final Response response = keycloak.realm(realmName)
                .groups()
                .add(group)) {
            if (response.getStatus() == 409) {
                Log.errorf("Could not import group for realm '%s': %s", realmName,
                        extractError(response).getErrorMessage());
            } else {
                Log.infof("Group '%s' imported for realm '%s'.", group.getName(), realmName);
            }
        } catch (final ClientErrorException e) {
            Log.errorf("Could not import group for realm '%s': %s", realmName, e.getMessage());
            return null;
        }

        final GroupRepresentation importedGroup = keycloak.realm(realmName)
                .groups()
                .groups(group.getName(), Boolean.TRUE, null, null, false)
                .getFirst();
        Log.infof("Loaded imported group '%s' from realm '%s'.", importedGroup.getName(),
                realmName);

        applyGroupHierarchy(realmName, group.getPath(), importedGroup);

        return importedGroup;
    }

    /**
     * This implements a naive approach of creating the group hierarchy for the given set of group configurations.
     * It assumes that the groups are imported in the correct order in regard to their hierarchy and the hierarchy
     * is linear as well.
     */
    private void applyGroupHierarchy(final String realmName, final String path, final GroupRepresentation group) {
        if (StringUtil.isNullOrEmpty(path)) {
            return;
        }

        final String[] groupHierarchy = path.split("/");
        if (groupHierarchy.length > 2) {
            final String groupName = groupHierarchy[groupHierarchy.length - 2];
            final Optional<GroupRepresentation> potentialGroup = keycloak.realm(realmName)
                    .groups()
                    .query(groupName, false, null, null, false)
                    .stream()
                    .filter(grp -> grp.getName().equals(groupName))
                    .findFirst();
            if (potentialGroup.isPresent()) {
                final GroupRepresentation parentGroup = potentialGroup.get();
                try (final Response response = keycloak.realm(realmName)
                        .groups()
                        .group(parentGroup.getId())
                        .subGroup(group)) {
                    Log.infof("Adding group '%s' as child to '%s'.", group.getName(), parentGroup.getName());
                }
            }
        }
    }
}
