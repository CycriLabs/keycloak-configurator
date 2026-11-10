package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import jakarta.enterprise.context.ApplicationScoped;

import org.keycloak.admin.client.resource.RoleMappingResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ClientScopeMappingDTO;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ImporterStatus;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

/**
 * Adds realm and client roles to the scope of a client (Clients → client → Client scopes →
 * dedicated scope → Scope). The client is taken from the directory (or flat file) name. Like the
 * other role mapping importers, it only adds mappings and never removes existing ones.
 */
@ApplicationScoped
public class ClientScopeMappingImporter extends AbstractImporter<ClientScopeMappingDTO> {
    @Override
    public EntityType getType() {
        return EntityType.CLIENT_SCOPE_MAPPING;
    }

    @Override
    protected ClientScopeMappingDTO loadEntity(final ConfigurationFile file) {
        final ClientScopeMappingDTO entity = loadEntity(file, ClientScopeMappingDTO.class);
        if (configuration.isDryRun()) {
            Log.infof("Loaded scope mappings of client '%s' from file '%s'.", file.getClientId(),
                    file.getFile());
        }
        return entity;
    }

    @Override
    protected ClientScopeMappingDTO executeImport(final ConfigurationFile file,
            final ClientScopeMappingDTO scopeMapping) {
        final String realmName = file.getRealmName();
        final String clientId = file.getClientId();
        Log.debugf("Importing scope mappings '%s' of client '%s' of realm '%s'.",
                file.getFile().getFileName(), clientId, realmName);

        final ClientRepresentation client = keycloakCache.getClientByClientId(realmName, clientId);
        if (client == null) {
            setStatus(ImporterStatus.FAILURE);
            Log.errorf("Could not find client '%s' of realm '%s'. Skipping scope mappings '%s'.",
                    clientId, realmName, file.getFile());
            return null;
        }

        final RoleMappingResource scopeMappings = keycloak.realm(realmName)
                .clients()
                .get(client.getId())
                .getScopeMappings();
        importRealmRoles(scopeMapping.getRealm(), realmName, clientId, scopeMappings);
        importClientRoles(scopeMapping.getClient(), realmName, clientId, scopeMappings);
        return null;
    }

    private void importRealmRoles(final List<String> roleNames, final String realmName,
            final String clientId, final RoleMappingResource scopeMappings) {
        if (roleNames == null || roleNames.isEmpty()) {
            return;
        }

        final List<RoleRepresentation> roles = new ArrayList<>();
        for (final String roleName : roleNames) {
            final RoleRepresentation role = keycloakCache.getRoleByName(realmName, roleName);
            if (role == null) {
                setStatus(ImporterStatus.FAILURE);
                Log.errorf("Could not find realm role '%s' of realm '%s' for the scope of client '%s'.",
                        roleName, realmName, clientId);
            } else {
                roles.add(role);
            }
        }

        if (!roles.isEmpty()) {
            scopeMappings.realmLevel().add(roles);
            Log.infof("Added realm roles %s to the scope of client '%s' of realm '%s'.",
                    names(roles), clientId, realmName);
        }
    }

    private void importClientRoles(final Map<String, List<String>> rolesByClient,
            final String realmName, final String clientId, final RoleMappingResource scopeMappings) {
        if (rolesByClient == null || rolesByClient.isEmpty()) {
            return;
        }

        for (final Map.Entry<String, List<String>> entry : rolesByClient.entrySet()) {
            final String roleClientId = entry.getKey();
            final List<String> roleNames = entry.getValue();
            if (roleNames == null || roleNames.isEmpty()) {
                continue;
            }

            final ClientRepresentation roleClient =
                    keycloakCache.getClientByClientId(realmName, roleClientId);
            if (roleClient == null) {
                setStatus(ImporterStatus.FAILURE);
                Log.errorf("Could not find client '%s' of realm '%s' for the scope of client '%s'.",
                        roleClientId, realmName, clientId);
                continue;
            }

            final List<RoleRepresentation> roles = new ArrayList<>();
            for (final String roleName : roleNames) {
                final RoleRepresentation role =
                        keycloakCache.getClientRoleByName(realmName, roleClientId, roleName);
                if (role == null) {
                    setStatus(ImporterStatus.FAILURE);
                    Log.errorf(
                            "Could not find client role '%s' of client '%s' of realm '%s' for the scope of client '%s'.",
                            roleName, roleClientId, realmName, clientId);
                } else {
                    roles.add(role);
                }
            }

            if (!roles.isEmpty()) {
                scopeMappings.clientLevel(roleClient.getId()).add(roles);
                Log.infof("Added client roles %s of client '%s' to the scope of client '%s' of realm '%s'.",
                        names(roles), roleClientId, clientId, realmName);
            }
        }
    }

    private static List<String> names(final List<RoleRepresentation> roles) {
        return roles.stream()
                .map(RoleRepresentation::getName)
                .toList();
    }
}
