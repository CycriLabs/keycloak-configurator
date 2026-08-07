package com.cycrilabs.keycloak.configurator.commands.diff.boundary;

import jakarta.enterprise.context.ApplicationScoped;

import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ServiceUserRealmRoleMappingDTO;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

/**
 * Compares the realm roles assigned to a service account against the local configuration.
 * <p>
 * The comparison is bidirectional: roles that are assigned on the server but not configured
 * locally are reported as well, because the local file declares the complete set of realm roles
 * of the service account.
 */
@ApplicationScoped
public class ServiceAccountRealmRoleDiffer extends AbstractDiffer<ServiceUserRealmRoleMappingDTO> {
    @Override
    public EntityType getType() {
        return EntityType.SERVICE_ACCOUNT_REALM_ROLE;
    }

    @Override
    protected ServiceUserRealmRoleMappingDTO loadEntity(final ConfigurationFile file) {
        return entityLoader.loadEntity(file.getFile(), ServiceUserRealmRoleMappingDTO.class);
    }

    @Override
    protected String getEntityName(final ConfigurationFile file,
            final ServiceUserRealmRoleMappingDTO local) {
        return file.getServiceUsername();
    }

    @Override
    protected ServiceUserRealmRoleMappingDTO findServerEntity(final ConfigurationFile file,
            final ServiceUserRealmRoleMappingDTO local) {
        final String realm = file.getRealmName();
        final UserRepresentation serviceUser =
                findUserByExactUsername(realm, file.getServiceUsername());
        if (serviceUser == null) {
            return null;
        }

        final ServiceUserRealmRoleMappingDTO assignedMapping = new ServiceUserRealmRoleMappingDTO();
        assignedMapping.setRoles(keycloak.realm(realm)
                .users()
                .get(serviceUser.getId())
                .roles()
                .realmLevel()
                .listAll()
                .stream()
                .map(RoleRepresentation::getName)
                .filter(role -> !builtInEntityFilter.isAutoAssignedRole(realm, role))
                .toList());
        return assignedMapping;
    }
}
