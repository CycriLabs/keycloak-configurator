package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import java.nio.file.Path;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.Response;

import org.keycloak.representations.idm.ComponentRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ImporterStatus;
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
        final ComponentRepresentation component = loadEntity(file, ComponentRepresentation.class);

        final String[] fileNameParts = file.toString().split(PATH_SEPARATOR);
        final String realmName = fileNameParts[fileNameParts.length - 3];

        if (component.getParentId() != null && !component.getParentId().equals(realmName)) {
            final ComponentRepresentation parent =
                    findComponentByName(realmName, component.getParentId());
            if (parent == null) {
                setStatus(ImporterStatus.FAILURE);
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
            if (isConflict(response)) {
                Log.infof("Could not import component from file for realm '%s': %s", realmName,
                        extractError(response).getErrorMessage());
            } else {
                Log.infof("Component '%s' imported for realm '%s'.", component.getName(),
                        realmName);
            }
        } catch (final ClientErrorException e) {
            setStatus(ImporterStatus.FAILURE);
            Log.errorf("Could not import component from file for realm '%s': %s", realmName,
                    e.getMessage());
            return null;
        }

        return findComponentByName(realmName, component.getName());
    }

    private ComponentRepresentation findComponentByName(final String realmName, final String name) {
        final Optional<ComponentRepresentation> importedComponent = keycloak.realm(realmName)
                .components()
                .query(realmName)
                .stream()
                .filter(c -> c.getName().equals(name))
                .findFirst();
        return importedComponent.orElse(null);
    }
}
