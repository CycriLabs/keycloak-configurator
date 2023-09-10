package com.cycrilabs.eam.keycloak.configurator.shared.control;

import com.cycrilabs.eam.keycloak.configurator.commands.configure.control.ConfigureCommand;
import com.cycrilabs.eam.keycloak.configurator.commands.secrets.control.ExportSecretsCommand;

import io.quarkus.picocli.runtime.annotations.TopCommand;
import picocli.CommandLine;

@TopCommand
@CommandLine.Command(mixinStandardHelpOptions = true,
        version = "0.1.0",
        subcommands = { ConfigureCommand.class, ExportSecretsCommand.class })
public class EntryCommand {
    @CommandLine.Option(required = true, names = { "-s", "--server" },
            description = "Keycloak server that will be configured.")
    String server = "";
    @CommandLine.Option(required = true, names = { "-u", "--username" },
            description = "Username of the admin user that is used for configuration.")
    String username = "";
    @CommandLine.Option(required = true, names = { "-p", "--password" },
            description = "Password of the admin user that is used for configuration.")
    String password = "";
}
