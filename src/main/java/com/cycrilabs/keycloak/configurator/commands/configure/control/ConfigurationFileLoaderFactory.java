package com.cycrilabs.keycloak.configurator.commands.configure.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;

import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigureCommandConfiguration;

import io.quarkus.logging.Log;

@ApplicationScoped
public class ConfigurationFileLoaderFactory {
    @Inject
    ConfigureCommandConfiguration configuration;

    @Produces
    public ConfigurationFileLoader create() {
        final ConfigurationFileLoader loader = createLoader();
        Log.infof("Using '%s'.", loader.getClass().getSimpleName());
        return loader;
    }

    private ConfigurationFileLoader createLoader() {
        if (configuration.isFlatFiles()) {
            return new FlatFileConfigurationFileLoader(configuration);
        } else {
            return new DirectoryConfigurationFileLoader(configuration);
        }
    }
}
