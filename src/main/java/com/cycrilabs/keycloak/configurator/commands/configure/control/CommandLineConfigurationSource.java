package com.cycrilabs.keycloak.configurator.commands.configure.control;

import lombok.Getter;

import com.cycrilabs.keycloak.configurator.shared.entity.ConfigurationSource;

import picocli.CommandLine.ParseResult;

/**
 * A {@link ConfigurationSource} that reads its values directly from the parsed command line.
 * This keeps the configuration file loading infrastructure independent of the invoked
 * sub-command: every sub-command that reads configuration files declares the options
 * {@code -c} and {@code --flat-files}, which is all that is required here.
 */
@Getter
public class CommandLineConfigurationSource implements ConfigurationSource {
    private final String configDirectory;
    private final boolean flatFiles;

    public CommandLineConfigurationSource(final ParseResult parseResult) {
        configDirectory = getMatchedOption(parseResult, "-c");
        flatFiles = this.<Boolean>getMatchedOption(parseResult, "--flat-files").booleanValue();
    }

    private <T> T getMatchedOption(final ParseResult parseResult, final String name) {
        return parseResult.subcommand().commandSpec().optionsMap().get(name).getValue();
    }
}
