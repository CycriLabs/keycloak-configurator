package com.cycrilabs.keycloak.configurator.commands.diff.boundary;

import java.util.ArrayList;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ServiceUserClientRoleMappingDTO;
import com.cycrilabs.keycloak.configurator.commands.diff.entity.Difference;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;
import com.fasterxml.jackson.core.type.TypeReference;

import io.quarkus.logging.Log;

/**
 * Compares the client roles assigned to a service account against the local configuration.
 * <p>
 * The comparison is bidirectional for the declared clients: roles that are assigned on the server
 * but not configured locally are reported as well, because the local file declares the complete
 * set of roles for its client.
 */
@ApplicationScoped
public class ServiceAccountClientRoleDiffer
        extends AbstractDiffer<List<ServiceUserClientRoleMappingDTO>> {
    @Override
    public EntityType getType() {
        return EntityType.SERVICE_ACCOUNT_CLIENT_ROLE;
    }

    @Override
    protected List<ServiceUserClientRoleMappingDTO> loadEntity(final ConfigurationFile file) {
        return entityLoader.loadEntity(file.getFile(), new TypeReference<>() { });
    }

    @Override
    protected String getEntityName(final ConfigurationFile file,
            final List<ServiceUserClientRoleMappingDTO> local) {
        return file.getServiceUsername();
    }

    /**
     * Compares every configured mapping separately, so that the report names the client whose
     * roles differ.
     */
    @Override
    protected void diffEntity(final ConfigurationFile file) {
        final List<ServiceUserClientRoleMappingDTO> local = loadEntity(file);
        final String realm = getRealmName(file, local);
        final String serviceUsername = getEntityName(file, local);
        reporter.entityChecked();

        final List<ServiceUserClientRoleMappingDTO> server = findServerEntity(file, local);
        if (server == null) {
            reporter.add(Difference.missingOnServer(getType(), realm, serviceUsername));
            return;
        }

        for (int i = 0; i < local.size(); i++) {
            final ServiceUserClientRoleMappingDTO localMapping = local.get(i);
            final String entity = serviceUsername + " -> " + localMapping.getClient();
            final ServiceUserClientRoleMappingDTO serverMapping = server.get(i);
            if (serverMapping == null) {
                reporter.add(Difference.missingOnServer(getType(), realm, entity));
                continue;
            }
            compare(realm, entity, localMapping, serverMapping);
        }
    }

    /**
     * Reads the client roles actually assigned to the service account. The result holds one entry
     * per locally configured mapping, in the same order, and null where the client does not exist.
     */
    @Override
    protected List<ServiceUserClientRoleMappingDTO> findServerEntity(final ConfigurationFile file,
            final List<ServiceUserClientRoleMappingDTO> local) {
        final String realm = file.getRealmName();
        final UserRepresentation serviceUser =
                findUserByExactUsername(realm, file.getServiceUsername());
        if (serviceUser == null) {
            return null;
        }

        final List<ServiceUserClientRoleMappingDTO> assignedMappings = new ArrayList<>();
        for (final ServiceUserClientRoleMappingDTO mapping : local) {
            assignedMappings.add(readAssignedRoles(realm, serviceUser, mapping.getClient()));
        }
        return assignedMappings;
    }

    private ServiceUserClientRoleMappingDTO readAssignedRoles(final String realm,
            final UserRepresentation serviceUser, final String clientId) {
        final ClientRepresentation client = keycloakCache.getClientByClientId(realm, clientId);
        if (client == null) {
            Log.debugf("Client '%s' of realm '%s' does not exist.", clientId, realm);
            return null;
        }

        final ServiceUserClientRoleMappingDTO assignedMapping =
                new ServiceUserClientRoleMappingDTO();
        assignedMapping.setClient(clientId);
        assignedMapping.setRoles(keycloak.realm(realm)
                .users()
                .get(serviceUser.getId())
                .roles()
                .clientLevel(client.getId())
                .listAll()
                .stream()
                .map(RoleRepresentation::getName)
                .toList());
        return assignedMapping;
    }
}
