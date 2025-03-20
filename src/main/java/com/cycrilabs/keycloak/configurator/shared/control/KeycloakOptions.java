package com.cycrilabs.keycloak.configurator.shared.control;

import picocli.CommandLine;

public class KeycloakOptions {
    @CommandLine.Option(required = true, names = { "-s", "--server" },
            description = "Keycloak server that will be configured.",
            scope = CommandLine.ScopeType.INHERIT)
    String server = "";
    @CommandLine.Option(required = true, names = { "-u", "--username" },
            description = "Username of the admin user that is used for configuration.",
            scope = CommandLine.ScopeType.INHERIT)
    String username = "";
    @CommandLine.Option(required = true, names = { "-p", "--password" },
            description = "Password of the admin user that is used for configuration.",
            scope = CommandLine.ScopeType.INHERIT, arity = "0..1", interactive = true)
    String password = "";
    @CommandLine.Option(names = { "--dry-run" },
            description = "If set, the configuration will not be applied to the server.",
            scope = CommandLine.ScopeType.INHERIT)
    boolean dryRun = false;
}
