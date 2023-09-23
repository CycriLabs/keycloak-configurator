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

### Sub-Command `configure`

The `configure` sub-command allows to configure a Keycloak instance with a set of realms, clients, client-roles, etc.
The configuration is done by providing a set of configuration files. Each configuration file is a JSON file that represents
a entity of the Keycloak REST API.

TODO:
- list global options and sub command options
- describe configuration files

### Sub-Command `export-secrets`

TODO: describe sub command

## Development

The configurator can be started in dev mode using the Quarkus CLI and passing the arguments via `-Dquarkus.args`.
For example, showing the help can be done as follows:

```bash
mvn quarkus:dev "-Dquarkus.args=-h"
```

Starting the import of a configuration can be done as follows:

```bash
mvn quarkus:dev "-Dquarkus.args=configure -s http://localhost:40800 -u keycloak -p root -c ../keycloak-configuration-eam"
```

The help of a sub-command is shown as follows:

```bash
mvn quarkus:dev "-Dquarkus.args=configure -h"
```

## Execution examples

- Show help:
    ```bash
    mvn quarkus:dev "-Dquarkus.args=-h"
    ```
- Show help of the `configure` sub-command:
    ```bash
    mvn quarkus:dev "-Dquarkus.args=configure -h"
    ```
- Execute configuration import:
    ```bash
    mvn quarkus:dev "-Dquarkus.args=configure -s http://localhost:40800 -u keycloak -p root -c ../keycloak-configuration-eam"
    ```
- Show help of the `export-secrets` sub-command:
    ```bash
    mvn quarkus:dev "-Dquarkus.args=export-secrets -h"
    ```
- Export client secrets of all clients of the realm `eam`:
    ```bash
    mvn quarkus:dev "-Dquarkus.args=export-secrets -s http://localhost:40800 -u keycloak -p root -r eam"
    ```

Specify the log level via `-Dquarkus.log.level`. For example, to set the log level to `INFO`:

```bash
mvn quarkus:dev "-Dquarkus.args=export-secrets -s http://localhost:40800 -u keycloak -p root -r eam" "-Dquarkus.log.level=INFO"
```
