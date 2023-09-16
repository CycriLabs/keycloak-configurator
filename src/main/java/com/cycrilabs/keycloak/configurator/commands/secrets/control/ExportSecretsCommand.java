package com.cycrilabs.keycloak.configurator.commands.secrets.control;

import io.quarkus.logging.Log;
import picocli.CommandLine;

@CommandLine.Command(name = "export-secrets", mixinStandardHelpOptions = true)
public class ExportSecretsCommand implements Runnable {
    @CommandLine.Option(required = true, names = { "-s", "--server" },
            description = "Keycloak server that will be configured.")
    String target = "";
    @CommandLine.Option(required = true, names = { "-u", "--username" },
            description = "Username of the admin user that is used for configuration.")
    String username = "";
    @CommandLine.Option(required = true, names = { "-p", "--password" },
            description = "Password of the admin user that is used for configuration.")
    String password = "";

    @Override
    public void run() {
        Log.infof("Fetching secrets from target %s.", target);
    }
}
