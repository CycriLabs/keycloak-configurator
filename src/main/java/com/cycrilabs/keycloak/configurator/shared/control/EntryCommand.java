package com.cycrilabs.keycloak.configurator.shared.control;

import com.cycrilabs.keycloak.configurator.commands.configure.control.ConfigureCommand;
import com.cycrilabs.keycloak.configurator.commands.export.control.ExportEntitiesCommand;
import com.cycrilabs.keycloak.configurator.commands.generate.control.GenerateSecretsCommand;
import com.cycrilabs.keycloak.configurator.commands.secrets.control.ExportSecretsCommand;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(mixinStandardHelpOptions = true, versionProvider = VersionProvider.class,
        subcommands = { ConfigureCommand.class, ExportEntitiesCommand.class,
                ExportSecretsCommand.class, GenerateSecretsCommand.class })
public class EntryCommand {
}
