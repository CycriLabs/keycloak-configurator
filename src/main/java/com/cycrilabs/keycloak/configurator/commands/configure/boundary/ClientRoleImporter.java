package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;

import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ImporterStatus;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class ClientRoleImporter extends AbstractImporter<RoleRepresentation> {
    @Override
    public EntityType getType() {
        return EntityType.CLIENT_ROLE;
    }

    @Override
    protected RoleRepresentation loadEntity(final ConfigurationFile file) {
        final RoleRepresentation entity = loadEntity(file, RoleRepresentation.class);
        if (configuration.isDryRun()) {
            Log.infof("Loaded client role '%s' from file '%s'.", entity.getName(), file.getFile());
        }
        return entity;
    }

    @Override
    protected RoleRepresentation executeImport(final ConfigurationFile file,
            final RoleRepresentation role) {
        final String realmName = file.getRealmName();
        final String clientId = file.getClientId();
        final ClientRepresentation client = keycloakCache.getClientByClientId(realmName, clientId);

        try {
            keycloak.realm(realmName)
                    .clients()
                    .get(client.getId())
                    .roles()
                    .create(role);
            Log.infof("Client role '%s' imported for client '%s' of realm '%s'.", role.getName(),
                    clientId, realmName);
        } catch (final ClientErrorException e) {
            if (isConflict(e.getResponse())) {
                Log.infof("Could not import client role for client '%s' of realm '%s': %s",
                        clientId, realmName, extractError(e).getErrorMessage());
            } else {
                setStatus(ImporterStatus.FAILURE);
                Log.errorf("Could not import client role for client '%s' of realm '%s': %s",
                        clientId, realmName, e.getMessage());
            }
        }

        return keycloakCache.getClientRoleByName(realmName, client.getClientId(), role.getName());
    }
}
