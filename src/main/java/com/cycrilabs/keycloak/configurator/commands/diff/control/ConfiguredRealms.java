package com.cycrilabs.keycloak.configurator.commands.diff.control;

import java.util.LinkedHashSet;
import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.keycloak.representations.idm.RealmRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.control.ConfigurationFileStore;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationFile;
import com.cycrilabs.keycloak.configurator.shared.control.ConfigurationEntityLoader;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

import io.quarkus.logging.Log;

/**
 * The set of realms that the local configuration describes.
 * <p>
 * Entities that exist on the server without a local configuration file are only reported for
 * these realms. A server usually hosts realms that are entirely unrelated to the configuration
 * at hand, and descending into them would produce hundreds of findings that say nothing about
 * the configuration being verified.
 */
@ApplicationScoped
public class ConfiguredRealms {
    @Inject
    ConfigurationFileStore configurationFileStore;
    @Inject
    ConfigurationEntityLoader entityLoader;

    private Set<String> realms;

    /**
     * The names of all realms described by the local configuration.
     *
     * @return the configured realm names
     */
    public Set<String> get() {
        if (realms == null) {
            realms = resolve();
            Log.debugf("Local configuration describes realms %s.", realms);
        }
        return realms;
    }

    public boolean contains(final String realm) {
        return realm != null && get().contains(realm);
    }

    private Set<String> resolve() {
        final Set<String> configuredRealms = new LinkedHashSet<>();
        for (final EntityType entityType : EntityType.values()) {
            for (final ConfigurationFile file : configurationFileStore.getImportFiles(entityType)) {
                if (file.getRealmName() != null) {
                    configuredRealms.add(file.getRealmName());
                }
                if (entityType == EntityType.REALM) {
                    // Realm files carry no realm name when read from a nested directory
                    // structure, so the name is taken from the file content itself.
                    addRealmNameFromFile(configuredRealms, file);
                }
            }
        }
        return configuredRealms;
    }

    private void addRealmNameFromFile(final Set<String> configuredRealms,
            final ConfigurationFile file) {
        try {
            final RealmRepresentation realm =
                    entityLoader.loadEntity(file.getFile(), RealmRepresentation.class);
            if (realm.getRealm() != null) {
                configuredRealms.add(realm.getRealm());
            }
        } catch (final RuntimeException e) {
            Log.warnf("Could not determine realm name from file '%s': %s", file.getFile(),
                    e.getMessage());
        }
    }
}
