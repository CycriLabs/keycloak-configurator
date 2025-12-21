package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ImporterStatus;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.ClientScopeRepresentation;

@ApplicationScoped
public class ClientScopeImporter extends AbstractImporter<ClientScopeRepresentation> {

    @Override
    public EntityType getType() {
        return EntityType.CLIENT_SCOPE;
    }

    @Override
    protected ClientScopeRepresentation loadEntity(final ConfigurationFile file) {
        final ClientScopeRepresentation entity = loadEntity(file, ClientScopeRepresentation.class);
        if (configuration.isDryRun()) {
            Log.infof("Loaded client scope '%s' from file '%s'.", entity.getName(), file.getFile());
        }
        return entity;
    }

    @Override
    protected ClientScopeRepresentation executeImport(final ConfigurationFile file,
            final ClientScopeRepresentation clientScope) {
        final String realmName = file.getRealmName();

        try (final Response response = keycloak.realm(realmName)
                .clientScopes()
                .create(clientScope)) {
            if (isConflict(response)) {
                Log.infof("Could not import client scope for realm '%s': %s", realmName,
                        extractError(response).getErrorMessage());
            } else {
                Log.infof("Client scope '%s' imported for realm '%s'.", clientScope.getName(), realmName);
            }
        } catch (final ClientErrorException e) {
            setStatus(ImporterStatus.FAILURE);
            Log.errorf("Could not import client scope for realm '%s': %s", realmName, e.getMessage());
        }

        final ClientScopeRepresentation importedClientScope =
                keycloakCache.getClientScopeByName(realmName, clientScope.getName());
        Log.infof("Loaded client scope '%s' from realm '%s'.", importedClientScope.getName(), realmName);
        return importedClientScope;
    }

}
