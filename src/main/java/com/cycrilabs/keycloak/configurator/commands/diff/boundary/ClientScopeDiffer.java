package com.cycrilabs.keycloak.configurator.commands.diff.boundary;

import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import org.keycloak.representations.idm.ClientScopeRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

@ApplicationScoped
public class ClientScopeDiffer extends AbstractDiffer<ClientScopeRepresentation> {
    @Override
    public EntityType getType() {
        return EntityType.CLIENT_SCOPE;
    }

    @Override
    protected ClientScopeRepresentation loadEntity(final ConfigurationFile file) {
        return entityLoader.loadEntity(file.getFile(), ClientScopeRepresentation.class);
    }

    @Override
    protected ClientScopeRepresentation findServerEntity(final ConfigurationFile file,
            final ClientScopeRepresentation local) {
        return keycloakCache.getClientScopeByName(file.getRealmName(), local.getName());
    }

    @Override
    protected String getEntityName(final ConfigurationFile file,
            final ClientScopeRepresentation local) {
        return local.getName();
    }

    @Override
    protected Set<String> getServerEntityNames(final String realm) {
        return keycloak.realm(realm)
                .clientScopes()
                .findAll()
                .stream()
                .map(ClientScopeRepresentation::getName)
                .collect(Collectors.toSet());
    }
}
