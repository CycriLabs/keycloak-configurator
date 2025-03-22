package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ImporterStatus;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ServiceUserClientRoleMappingDTO;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;
import com.fasterxml.jackson.core.type.TypeReference;

import io.quarkus.logging.Log;

@ApplicationScoped
public class ServiceAccountClientRoleImporter
        extends AbstractImporter<List<ServiceUserClientRoleMappingDTO>> {
    @Override
    public EntityType getType() {
        return EntityType.SERVICE_ACCOUNT_CLIENT_ROLE;
    }

    @Override
    protected List<ServiceUserClientRoleMappingDTO> loadEntity(final ConfigurationFile file) {
        final List<ServiceUserClientRoleMappingDTO> entity =
                loadEntity(file, new TypeReference<>() {
                });
        if (configuration.isDryRun()) {
            Log.infof("Loaded service account client roles from file '%s'.", file.getFile());
        }
        return entity;
    }

    @Override
    protected List<ServiceUserClientRoleMappingDTO> executeImport(final ConfigurationFile file,
            final List<ServiceUserClientRoleMappingDTO> serviceUserClientRoleMappings) {
        final String realmName = file.getRealmName();
        final String serviceUsername = file.getServiceUsername();

        Log.debugf(
                "Importing service account client roles '%s' for service user '%s' of realm '%s'.",
                file.getFile().getFileName(), serviceUsername, realmName);

        final UserRepresentation user = loadUserByUsername(realmName, serviceUsername);
        if (user == null) {
            return null;
        }

        Log.debugf("Found service user '%s' of realm '%s'.", user.getUsername(), realmName);

        importServiceUserClientRoleMappings(serviceUserClientRoleMappings, realmName, user);

        return null;
    }

    private UserRepresentation loadUserByUsername(final String realmName, final String username) {
        try {
            final List<UserRepresentation> userRepresentations = keycloak.realm(realmName)
                    .users()
                    .searchByUsername(username, Boolean.TRUE);
            if (userRepresentations.size() == 1) {
                return userRepresentations.getFirst();
            }

            Log.warnf("Found %d users '%s' of realm '%s'. Skipping import.",
                    Integer.valueOf(userRepresentations.size()), username, realmName);
        } catch (final Exception e) {
            setStatus(ImporterStatus.FAILURE);
            Log.errorf("Could not find user '%s' of realm '%s': %s", username, realmName,
                    e.getMessage());
        }
        return null;
    }

    private void importServiceUserClientRoleMappings(
            final List<ServiceUserClientRoleMappingDTO> serviceUserClientRoleMappings,
            final String realmName, final UserRepresentation serviceUser) {
        for (final ServiceUserClientRoleMappingDTO mapping : serviceUserClientRoleMappings) {
            final String clientName = mapping.getClient();
            final List<String> roles = mapping.getRoles();

            Log.debugf(
                    "Importing client roles '%s' of client '%s' for service user '%s' of realm '%s'.",
                    roles.toString(), clientName, serviceUser.getUsername(), realmName);
            final List<ClientRepresentation> clients = keycloak.realm(realmName)
                    .clients()
                    .findByClientId(clientName);
            if (clients.size() != 1) {
                Log.warnf("Found %d clients '%s' of realm '%s'. Skipping import.",
                        Integer.valueOf(clients.size()), clientName, realmName);
                return;
            }

            final ClientRepresentation client = clients.getFirst();
            final Map<String, RoleRepresentation> availableClientRoles = keycloak.realm(realmName)
                    .clients()
                    .get(client.getId())
                    .roles()
                    .list()
                    .stream()
                    .collect(Collectors.toMap(RoleRepresentation::getName, Function.identity()));
            Log.debugf("Found %d roles of client '%s' of realm '%s'.",
                    Integer.valueOf(availableClientRoles.size()), clientName, realmName);

            keycloak.realm(realmName)
                    .users()
                    .get(serviceUser.getId())
                    .roles()
                    .clientLevel(client.getId())
                    .add(roles.stream()
                            .filter(availableClientRoles::containsKey)
                            .map(availableClientRoles::get)
                            .toList());
        }
    }
}
