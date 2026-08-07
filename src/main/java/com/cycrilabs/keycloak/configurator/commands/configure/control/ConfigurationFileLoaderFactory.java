package com.cycrilabs.keycloak.configurator.commands.configure.control;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;

import com.cycrilabs.keycloak.configurator.shared.entity.ConfigurationSource;

import io.quarkus.logging.Log;
import picocli.CommandLine;

@ApplicationScoped
public class ConfigurationFileLoaderFactory {
    /**
     * Create the loader for the invoked sub-command. The configuration source is derived from
     * the parsed command line instead of a concrete command configuration, so that every
     * sub-command reading configuration files (e.g. 'configure' and 'diff') is served by this
     * single producer.
     *
     * @param parseResult
     *         the parsed command line
     * @return the loader matching the requested configuration file layout
     */
    @Produces
    public ConfigurationFileLoader create(final CommandLine.ParseResult parseResult) {
        final ConfigurationFileLoader loader =
                createLoader(new CommandLineConfigurationSource(parseResult));
        Log.infof("Using '%s'.", loader.getClass().getSimpleName());
        return loader;
    }

    private ConfigurationFileLoader createLoader(final ConfigurationSource configuration) {
        if (configuration.isFlatFiles()) {
            return new FlatFileConfigurationFileLoader(configuration);
        } else {
            return new DirectoryConfigurationFileLoader(configuration);
        }
    }
}
