package com.cycrilabs.keycloak.configurator.commands.diff.boundary;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.enterprise.context.ApplicationScoped;

import org.keycloak.representations.idm.ComponentRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

@ApplicationScoped
public class ComponentDiffer extends AbstractDiffer<ComponentRepresentation> {
    /**
     * The parent components referenced by the local configuration. Sub components are only
     * enumerated below these parents, because creating a component provider such as an LDAP
     * provider makes Keycloak create a whole set of sub components on its own.
     */
    private final Set<String> locallyReferencedParents = new LinkedHashSet<>();

    @Override
    public EntityType getType() {
        return EntityType.COMPONENT;
    }

    @Override
    protected ComponentRepresentation loadEntity(final ConfigurationFile file) {
        return entityLoader.loadEntity(file.getFile(), ComponentRepresentation.class);
    }

    /**
     * Looks up the component below the parent that the local configuration declares. Like the
     * importer, the local 'parentId' is interpreted as the name of the parent component, and an
     * absent parent means the component belongs to the realm itself.
     */
    @Override
    protected ComponentRepresentation findServerEntity(final ConfigurationFile file,
            final ComponentRepresentation local) {
        final String realm = file.getRealmName();
        if (local.getParentId() != null) {
            locallyReferencedParents.add(local.getParentId());
        }

        final String parentId = resolveParentId(realm, local.getParentId());
        if (parentId == null) {
            Log.debugf("Parent '%s' of component '%s' does not exist in realm '%s'.",
                    local.getParentId(), local.getName(), realm);
            return null;
        }
        return findComponentByName(realm, parentId, local.getName());
    }

    @Override
    protected String getEntityName(final ConfigurationFile file,
            final ComponentRepresentation local) {
        return local.getName();
    }

    /**
     * The local configuration references the parent by name while the server reports its
     * generated identifier, so the server value is translated back to the parent's name.
     */
    @Override
    protected ComponentRepresentation normalizeServerEntity(final ComponentRepresentation server,
            final ConfigurationFile file, final ComponentRepresentation local) {
        final String realm = file.getRealmName();
        if (local.getParentId() == null || server.getParentId() == null
                || server.getParentId().equals(getRealmId(realm))) {
            return server;
        }

        try {
            final ComponentRepresentation parent = keycloak.realm(realm)
                    .components()
                    .component(server.getParentId())
                    .toRepresentation();
            server.setParentId(parent.getName());
        } catch (final RuntimeException e) {
            Log.debugf("Could not resolve the parent of component '%s' in realm '%s': %s",
                    local.getName(), realm, e.getMessage());
        }
        return server;
    }

    @Override
    protected Set<String> getServerEntityNames(final String realm) {
        final String realmId = getRealmId(realm);
        if (realmId == null) {
            return Set.of();
        }

        final Set<String> names = new LinkedHashSet<>(getComponentNames(realm, realmId));
        for (final String parentName : locallyReferencedParents) {
            final ComponentRepresentation parent = findComponentByName(realm, realmId, parentName);
            if (parent != null) {
                names.addAll(getComponentNames(realm, parent.getId()));
            }
        }
        return names;
    }

    private Set<String> getComponentNames(final String realm, final String parentId) {
        return keycloak.realm(realm)
                .components()
                .query(parentId)
                .stream()
                .filter(component -> !builtInEntityFilter.isBuiltInComponentProviderType(
                        component.getProviderType()))
                .map(ComponentRepresentation::getName)
                .collect(Collectors.toSet());
    }

    /**
     * Resolves the identifier of the parent a component belongs to.
     *
     * @param realm
     *         the realm of the component
     * @param localParentName
     *         the parent as declared locally, or null if the component belongs to the realm
     * @return the identifier of the parent, or null if the declared parent does not exist
     */
    private String resolveParentId(final String realm, final String localParentName) {
        final String realmId = getRealmId(realm);
        if (localParentName == null || localParentName.equals(realmId)) {
            return realmId;
        }

        final ComponentRepresentation parent =
                findComponentByName(realm, realmId, localParentName);
        return parent == null
               ? null
               : parent.getId();
    }

    private ComponentRepresentation findComponentByName(final String realm, final String parentId,
            final String name) {
        return keycloak.realm(realm)
                .components()
                .query(parentId)
                .stream()
                .filter(component -> name.equals(component.getName()))
                .findFirst()
                .orElse(null);
    }

    private String getRealmId(final String realm) {
        final RealmRepresentation realmRepresentation = keycloakCache.getRealmByName(realm);
        return realmRepresentation == null
               ? null
               : realmRepresentation.getId();
    }
}
