# Keycloak Configurator

The Keycloak Configurator allows to __configure__ Keycloak based on a set of
realms, clients, client-roles, etc. represented by JSON files (configuration-as-code).
In addition, it allows to __export secrets__ of all clients of a given realm based
on a set of provided secret templates.
Furthermore, it is possible to __export the configuration__ of Keycloak back to JSON
files.

All communication happens via the
[Keycloak REST API](https://www.keycloak.org/docs-api/22.0.1/rest-api/index.html).
Internally, it is done via the
[Quarkus Keycloak Java Admin Client](https://quarkus.io/guides/security-keycloak-admin-client).

## Versions

The configurator supports the following Keycloak API:

| Tool Version | Quarkus Version | Keycloak Version |
|--------------|-----------------|------------------|
| 1.0.x        | 3.6.3           | 23.0.0           |
| 1.1.x        | 3.6.8           | 23.0.0           |
| 1.2.x        | 3.8.1           | 23.0.7           |
| 1.3.x        | 3.9.2           | 23.0.7           |
| 1.4.x        | 3.9.2           | 23.0.7           |
| 1.5.x        | 3.15.1          | 25.0.6           |
| 1.6.x        | 3.19.1          | 26.1.3           |

## Usage

The configurator prints out the help when executed with `-h` or `--help`. It
lists all commands that are available. Currently, four commands are supported:

* `configure` - Configures a Keycloak instance with a set of realms, clients, client-roles, etc.
* `export-secrets` - Exports secrets of all clients of the given realm
* `rotate-secrets` - Rotates secrets of all clients of the given realm
* `export-entities` - Exports entities of the given realm, optionally filtered by type or name

Each command supports a set of options that can be listed by executing it with `-h` or `--help`.

### Sub-Command `configure`

The `configure` sub-command allows to configure a Keycloak instance based on a set
of realms, clients, client-roles, etc. JSON configuration files. Each file represents
an entity of the Keycloak REST API. The following directory structure is expected:

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

The directory may contain multiple subdirectories. Each subdirectory
represents one realm to be imported together with the respective configuration.
All directories containing one of the keywords shown above are treated as
configuration input.

The following table lists all required and optional options of the `configure`
sub-command:

| Option                | Required | Description                                                                                       |
|-----------------------|----------|---------------------------------------------------------------------------------------------------|
| `-s`, `--server`      | yes      | The URL of the Keycloak server.                                                                   |
| `-u`, `--username`    | yes      | The username of the Keycloak admin user.                                                          |
| `-p`, `--password`    | yes      | The password of the Keycloak admin user. Can be omitted and read in via user input.               |
| `-c`, `--config`      | yes      | The path to the directory containing the configuration files.                                     |
| `-t`, `--entity-type` | no       | Allows to import only on specific entity type. Requires all prerequisites to be imported already. |
| `--exit-on-error`     | no       | Exits the application directly when a configuration command occurs.                               |

The following table lists all supported entity types and the corresponding directory
identifier to identifier configuration source directories:

| Entity Type                   | Directory identifier           | Description                            |
|-------------------------------|--------------------------------|----------------------------------------|
| `realm`                       | `realms`                       | A realm.                               |
| `client`                      | `clients`                      | A client.                              |
| `client-role`                 | `client-roles`                 | A client role.                         |
| `realm-role`                  | `realm-roles`                  | A realm role.                          |
| `service-account-client-role` | `service-account-client-roles` | A service account client role mapping. |
| `group`                       | `groups`                       | A group.                               |
| `user`                        | `users`                        | A user.                                |
| `service-account-realm-role`  | `service-account-realm-roles`  | A service account realm role mapping.  |
| `component`                   | `components`                   | A component, e.g. LDAP provider.       |

Entities are imported as listed in the table above.

The `configure`-command allows expanding a given set of variables. The following set
of variables is supported:

| Variable | Description                                                                                                                                        |
|----------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| `env`    | A map of environment variables that start with the prefix `kcc`. The key of the map is the environment variable name based on Microprofile schema. |

To access environment variables, they must be prefixed with `kcc`. For example, to access
the environment variable `KCC_LDAP_BIND_PW=password`, the following can be used:

```json
{
    "type": "password",
    "value": "$env.KCC_LDAP_BIND_PW"
}
```

### Sub-Command `export-secrets`

The `export-secrets` sub-command allows to export secrets of all clients of the
given realm. The secrets are exported based on a set of provided secret templates.

The following table lists all required and optional options of the `export-secrets`
sub-command:

| Option               | Required | Description                                                                               |
|----------------------|----------|-------------------------------------------------------------------------------------------|
| `-s`, `--server`     | yes      | The URL of the Keycloak server.                                                           |
| `-u`, `--username`   | yes      | The username of the Keycloak admin user.                                                  |
| `-p`, `--password`   | yes      | The password of the Keycloak admin user. Can be omitted and read in via user input.       |
| `-r`, `--realm`      | yes      | The realm to export the secrets from.                                                     |
| `-c`, `--config`     | yes      | The path to the directory containing the secret templates.                                |
| `-o`, `--output`     | no       | The path to the directory where the secrets are exported to. Defaults to the working dir. |
| `-n`, `--client-ids` | no       | A comma-separated list of client ids to export the secrets from.                          |

The secret templates support expanding a given set of variables. The following set
of variables is supported:

| Variable          | Description                                                                                                                                        |
|-------------------|----------------------------------------------------------------------------------------------------------------------------------------------------|
| `auth_server_url` | The URL of the Keycloak server.                                                                                                                    |
| `realm`           | The realm of the client.                                                                                                                           |
| `client_id`       | The client id of the client.                                                                                                                       |
| `client`          | The Keycloak representation object of the current client being exported.                                                                           |
| `secret`          | The secret of the client.                                                                                                                          |
| `clients`         | The map of Keycloak representation objects of all clients belonging to the realm. The key of the map is the `clientId` of each client.             |
| `env`             | A map of environment variables that start with the prefix `kcc`. The key of the map is the environment variable name based on Microprofile schema. |

Variables must be placed within the secret template as `$variable`. For example:

```properties
QUARKUS_OIDC_CREDENTIALS_SECRET=$secret
```

The `client` variable is the Keycloak representation [ClientRepresentation](https://www.keycloak.org/docs-api/23.0.6/rest-api/#ClientRepresentation).
All fields can be accessed as described in [Velocity References](https://velocity.apache.org/engine/2.3/user-guide.html#references),
for example:

```properties
QUARKUS_OIDC_CREDENTIALS_SECRET=$client.secret
```

To access environment variables, they must be prefixed with `kcc`. For example, to access
the environment variable `KCC_DATABASE_NAME=database`, the following can be used:

```properties
DATASOURCE_JDBC_URL=jdbc:postgresql://$env.KCC_DATABASE_NAME:5432
```

The same approach applies to the `clients` variable. For example, to access the secret
of a different client than the current one, the following can be used:

```properties
QUARKUS_OIDC_CREDENTIALS_SECRET=$secret
QUARKUS_OIDC_IDENTITY_SERVICE_SECRET=$clients["identity-service"].secret
```

The `client_id` variable can be used within the filename of secret templates as well.
In comparison to the usage in templates, the `$` must be omitted.
Each client is exported to a separate file then. Otherwise, multiple clients will
overwrite each other. For example, a possible name could be `client_id-oidc.env`.
If there are two clients, `client-a` and `client-b`, the following files are created:

- `client-a-oidc.env`
- `client-b-oidc.env`

It is possible to provide multiple secret templates at once. The configurator will
generate a file for each client and secret template combination. If a secret template
has the same name as an expanded file would have, this file has precedence. For example,
if the following secrets templates are provided for the clients `client-a` and `client-b`:

- `client-a-oidc.env`
- `client_id-oidc.env`
- `client_id-oidc.json`

The following files are created:

- `client-a-oidc.env` (based on `client-a-oidc.env`)
- `client-b-oidc.env` (based on `client_id-oidc.env`)
- `client-a-oidc.json` (based on `client_id-oidc.json`)
- `client-b-oidc.json` (based on `client_id-oidc.json`)

### Sub-Command `rotate-secrets`

The `rotate-secrets` sub-command allows to rotate secrets of all clients of the
given realm.

The following table lists all required and optional options of the `rotate-secrets`
sub-command:

| Option             | Required | Description                                                                         |
|--------------------|----------|-------------------------------------------------------------------------------------|
| `-s`, `--server`   | yes      | The URL of the Keycloak server.                                                     |
| `-u`, `--username` | yes      | The username of the Keycloak admin user.                                            |
| `-p`, `--password` | yes      | The password of the Keycloak admin user. Can be omitted and read in via user input. |
| `-r`, `--realm`    | yes      | The realm to export the secrets from.                                               |
| `-c`, `--client`   | no       | Optionally, a client can be provided. Only the secret of this client is rotated.    |

### Sub-Command `export-entities`

The `export-entities` sub-command allows to export entities from Keycloak. The following
table lists all required and optional options of the `export-entities` sub-command:

| Option                | Required | Description                                                                                |
|-----------------------|----------|--------------------------------------------------------------------------------------------|
| `-s`, `--server`      | yes      | The URL of the Keycloak server.                                                            |
| `-u`, `--username`    | yes      | The username of the Keycloak admin user.                                                   |
| `-p`, `--password`    | yes      | The password of the Keycloak admin user. Can be omitted and read in via user input.        |
| `-r`, `--realm`       | no       | The realm to export the entities from.                                                     |
| `-c`, `--client`      | no       | The client to export the entities from.                                                    |
| `-t`, `--entity-type` | no       | The type of the entities to export.                                                        |
| `-n`, `--entity-name` | no       | The name of the entity to export.                                                          |
| `-o`, `--output`      | no       | The path to the directory where the entities are exported to. Defaults to the working dir. |

If no realm is provided, all realms are exported. If no client is provided, all clients
of the given realm are exported. If no entity type is provided, all entity types are exported.
If no entity name is provided, all entities of the given type are exported.

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

When trying to mount a volume on Windows Git Bash, e.g. the following error may occur:

```
Unmatched arguments from index 11: 'Files/Git/configuration', 'Files/Git/output'
```

This is caused by the `/` in the path. The forward slash must be noted as `//`. For example,
the following command mounts the directory `./secret-templates` and `./keycloak-secrets`
into the container as `/secret-templates` and `/output`. The configurator then
executes the `export-secrets` sub-command:

```bash
docker run \
    --mount type=bind,src="/$(pwd)/secret-templates",target="/secret-templates,readonly" \
    --mount type=bind,src="/$(pwd)/keycloak-secrets",target="/output" \
    --rm -it ghcr.io/cycrilabs/keycloak-configurator:latest export-secrets -s http://localhost:4080 -u keycloak -p root -r default -c //secret-templates -o //output
```

### Adapting the log-level

In same cases, it may be necessary to adapt the log-level of the configurator. This can be done
by appending `-Dquarkus.log.level=DEBUG` to the command. For example, the following command
executes the `export-secrets` sub-command with the log-level set to `INFO`:

```bash
docker run --rm -it ghcr.io/cycrilabs/keycloak-configurator:latest export-secrets -s http://localhost:4080 -u keycloak -p root -r default -c /secret-templates -o /output -Dquarkus.log.level=INFO
```

It is all possible to set the log-level specific to the configurator itself:

```bash
docker run --rm -it ghcr.io/cycrilabs/keycloak-configurator:latest export-secrets -s http://localhost:4080 -u keycloak -p root -r default -c /secret-templates -o /output -Dquarkus.log.category."com.cycrilabs".level=DEBUG
```

### Exporting secrets with JVM container images

When a container with JVM mode is executed to export secrets, the following error may pop up:

```
Failed to write file '/output/client.env'.
```

The application is run with the user `jboss` with ID `185`. Therefore, it is necessary to adapt
the output directory for the secrets to be owned by this user, e.g. for directory `services-env`:

```bash
sudo chown -R 185:185 services-env
```

## Development

The configurator can be started in dev mode using the Quarkus CLI and passing the arguments via `-Dquarkus.args`.
For example, starting the import of a configuration is done as follows:

```bash
mvn quarkus:dev "-Dquarkus.args=configure -s http://localhost:8080 -u keycloak -p root -c ../keycloak-configuration-eam/configuration"
```

Specify the log level via `-Dquarkus.log.level`. For example, to set the log level to `INFO`:

```bash
mvn quarkus:dev "-Dquarkus.args=export-secrets -s http://localhost:8080 -u keycloak -p root -r eam -c ../keycloak-configuration-eam/secret-templates -o out" "-Dquarkus.log.level=INFO"
```
