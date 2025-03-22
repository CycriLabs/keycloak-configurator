package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.GroupRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ImporterStatus;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;
import io.quarkus.runtime.util.StringUtil;

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
     * This implements a naive approach of creating the group hierarchy for the given set of group
     * configurations.
     * It assumes that the groups are imported in the correct order in regard to their hierarchy and
     * the hierarchy
     * is linear as well.
     */
    private void applyGroupHierarchy(final String realmName, final String path,
            final GroupRepresentation group) {
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
                    Log.infof("Adding group '%s' as child to '%s'.", group.getName(),
                            parentGroup.getName());
                }
            }
        }
    }
}
