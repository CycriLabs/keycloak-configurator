package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import java.nio.file.Path;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.ComponentRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;

import com.cycrilabs.keycloak.configurator.shared.control.JsonUtil;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class ComponentImporter extends AbstractImporter {
    @Override
    public EntityType getType() {
        return EntityType.COMPONENT;
    }

    @Override
    protected Object importFile(final Path file) {
        final ComponentRepresentation component =
                JsonUtil.loadEntity(file, ComponentRepresentation.class);

        final String[] fileNameParts = file.toString().split(PATH_SEPARATOR);
        final String realmName = fileNameParts[fileNameParts.length - 3];

        final RealmRepresentation realm = keycloak.realm(realmName).toRepresentation();
        if (component.getParentId() != null && !component.getParentId().equals(realm.getId())) {
            final ComponentRepresentation parent =
                    findComponentByName(realmName, realm.getId(), component.getParentId());
            if (parent == null) {
                Log.errorf(
                        "Could not import component from file '%s' for realm '%s' because of missing parent '%s'.",
                        file, realmName, component.getParentId());
                return null;
            }

            // adapt the parent id from a string to the real id of the parent
            component.setParentId(parent.getId());
        }

        try (final Response response = keycloak.realm(realmName)
                .components()
                .add(component)) {
            if (response.getStatus() == 409) {
                Log.errorf("Could not import component from file for realm '%s': %s", realmName,
                        extractError(response).getErrorMessage());
            } else {
                Log.infof("Component '%s' imported for realm '%s'.", component.getName(),
                        realmName);
            }
        } catch (final ClientErrorException e) {
            Log.errorf("Could not import component from file for realm '%s': %s", realmName,
                    e.getMessage());
            return null;
        }

        return findComponentByName(realmName, realm.getId(), component.getName());
    }

    private ComponentRepresentation findComponentByName(final String realmName,
            final String realmId, final String name) {
        final Optional<ComponentRepresentation> importedComponent = keycloak.realm(realmName)
                .components()
                .query(realmId)
                .stream()
                .filter(c -> name.equals(c.getName()))
                .findFirst();
        return importedComponent.orElse(null);
    }
}
