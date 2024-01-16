# Keycloak Configurator

The Keycloak Configurator allows to set up a Keycloak instance with a set of
realms, clients, client-roles, etc. The configuration is executed against the 
[Keycloak REST API](https://www.keycloak.org/docs-api/22.0.1/rest-api/index.html).
Communication is done via the 
[Quarkus Keycloak Java Admin Client](https://quarkus.io/guides/security-keycloak-admin-client).

## Usage

The configurator prints out the help when executed with `-h` or `--help`. It
lists all commands that are available. Currently, two commands are supported:

* `configure` - Configures a Keycloak instance with a set of realms, clients, client-roles, etc.
* `export-secrets` - Exports secrets of all clients of the given realm
* `rotate-secrets` - Rotates secrets of all clients of the given realm
* `export-entities` - Exports entities of the given realm, optionally filtered by type or name

Each commands support a set of options. The options can be listed by executing
the command with `-h` or `--help`.

### Sub-Command `configure`

The `configure` sub-command allows to configure a Keycloak instance with a set 
of realms, clients, client-roles, etc. The configuration is done by providing a
set of configuration files. Each configuration file is a JSON file that 
represents an entity of the Keycloak REST API. When calling the sub-command,
the path to the directory holding the configuration must be provided. The 
following structure is expected:

```
├── configuration
│   ├── realm-a
│   │   ├── realms
│   │   │   ├── realm-a.json
│   │   ├── clients
│   │   │   ├── client-a.json
│   │   ├── client-roles
│   │   │   ├── client-a
│   │   │   │   ├── role-a.json
│   │   ├── realm-roles
│   │   │   ├── ...
│   │   ├── groups
│   │   │   ├── ...
│   │   ├── users
│   │   │   ├── ...
│   ├── realm-b
│   │   ├── realms
│   │   │   ├── realm-b.json
│   │   ├── clients
│   │   ├── ...
```

The directory may container multiple subdirectories. Each subdirectory
represents one realm to be imported together with the respective configuration.
All directories containing one of the keywords shown above are treated as
configuration input.

TODO:

- list global options and sub command options
- describe configuration files

### Sub-Command `export-secrets`

TODO: describe sub command

### Sub-Command `rotate-secrets`

TODO: describe sub command

### Sub-Command `export-entities`

TODO: describe sub command

## Running via docker

The configurator can be run via docker. All container images are available at 
[GitHub Container Registry](https://github.com/CycriLabs/keycloak-configurator/pkgs/container/keycloak-configurator).

For example, the following command prints the version of the configurator.
After the version is printed, the container is stopped and removed:

```bash
docker run --rm -it ghcr.io/cycrilabs/keycloak-configurator:latest -V
```

### Providing configuration files

Several sub-commands require configuration files. The configuration files must
be provided to the container. This is done by mounting a volume to a
directory inside the container.

For example, the following command mounts the directory `./keycloak-configuration`
to the directory `/config` inside the container. The configurator is executed with
the `configure` sub-command and the configuration files are read from the
mounted volume `/config`:

```bash
docker run -v ./keycloak-configuration:/config --net="host" --pull=always --rm -it ghcr.io/cycrilabs/keycloak-configurator:latest configure -s http://localhost:4080 -u keycloak -p root -c /config
```

## Troubleshooting

### Mounting volumes on Windows Git Bash

When trying to mount a volume on Windows Git Bash, the following error may occur:

```
TODO add error message
```

This is caused by the `/` in the path. The forward slash must be noted as `//`. For example,
the following command mounts the directory `./secret-templates` and `./keycloak-secrets` to 
into the container as `/secret-templates` and `/output`, respectively. The configurator then
executes the `export-secrets` sub-command:

```bash
docker run \
    --mount type=bind,src="/$(pwd)/secret-templates",target="/secret-templates,readonly" \
    --mount type=bind,src="/$(pwd)/keycloak-secrets",target="/output" \
    --rm -it ghcr.io/cycrilabs/keycloak-configurator:latest export-secrets -s http://localhost:4080 -u keycloak -p root -r default -c //secret-templates -o //output
```

## Development

The configurator can be started in dev mode using the Quarkus CLI and passing the arguments via `-Dquarkus.args`.
For example, showing the help can be done as follows:

```bash
mvn quarkus:dev "-Dquarkus.args=-h"
```

Starting the import of a configuration can be done as follows:

```bash
mvn quarkus:dev "-Dquarkus.args=configure -s http://localhost:4080 -u keycloak -p root -c ../keycloak-configuration-eam"
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
    mvn quarkus:dev "-Dquarkus.args=configure -s http://localhost:4080 -u keycloak -p root -c ../keycloak-configuration-eam"
    ```
- Show help of the `export-secrets` sub-command:
    ```bash
    mvn quarkus:dev "-Dquarkus.args=export-secrets -h"
    ```
- Export client secrets of all clients of the realm `eam`:
    ```bash
    mvn quarkus:dev "-Dquarkus.args=export-secrets -s http://localhost:4080 -u keycloak -p root -r eam -c ./secret-templates"
    ```
- Export client entities of the realm `eam`:
    ```bash
    mvn quarkus:dev "-Dquarkus.args=export-entities -s http://localhost:4080 -u keycloak -p root -r eam -t client" "-Dquarkus.log.level=INFO"
    ```
- Export client entity "eam-js" of the realm `eam`:
    ```bash
    mvn quarkus:dev "-Dquarkus.args=export-entities -s http://localhost:4080 -u keycloak -p root -r eam -t client -n eam-js" "-Dquarkus.log.level=INFO"
    ```

Specify the log level via `-Dquarkus.log.level`. For example, to set the log level to `INFO`:

```bash
mvn quarkus:dev "-Dquarkus.args=export-secrets -s http://localhost:4080 -u keycloak -p root -r eam" "-Dquarkus.log.level=INFO"
```
