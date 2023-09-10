# Keycloak Configurator

The Keycloak Configurator allows to set up a Keycloak instance with a set of realms, clients, client-roles, etc.
The configuration is executed against the [Keycloak REST API](https://www.keycloak.org/docs-api/22.0.1/rest-api/index.html).
Communication is done via the [Quarkus Keycloak Java Admin Client](https://quarkus.io/guides/security-keycloak-admin-client).

## Usage

The configurator prints out the help when executed with `-h` or `--help`. It lists all commands that are available.
Currently, two commands are supported:

* `configure` - Configures a Keycloak instance with a set of realms, clients, client-roles, etc.
* `export-secrets` - Exports secrets of all clients of the given realm.

Each commands support a set of options. The options can be listed by executing the command with `-h` or `--help`.

TODO: list global options and sub command options

## Development

The configurator can be started in dev mode using the following commands. Depending on the goal, different commands can be used.

Showing the help can be done as follows:

```bash
mvn quarkus:dev -P github "-Dquarkus.args=-h"
```

Starting the import of a configuration can be done as follows:

```bash
mvn quarkus:dev -P github "-Dquarkus.args=-s http://localhost:40800 -u keycloak -p root configure -c C:\Users\MarcScheib\eam-projects\keycloak-configuration-eam"
```

Showing the help of a sub-command can be done as follows:

```bash
mvn quarkus:dev -P github "-Dquarkus.args=configure -h"
```
