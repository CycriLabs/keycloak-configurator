package com.cycrilabs.keycloak.configurator.commands.secrets.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.runtime.parser.ParseException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.ClientRepresentation;

import com.cycrilabs.keycloak.configurator.commands.secrets.entity.ExportSecretsCommandConfiguration;
import com.cycrilabs.keycloak.configurator.shared.control.JsonUtil;
import com.cycrilabs.keycloak.configurator.shared.control.VelocityUtils;

class SecretFilesGeneratorTest {
    private SecretFilesGenerator sut;
    private ExportSecretsCommandConfiguration configuration;

    @BeforeEach
    void setUp() {
        sut = new SecretFilesGenerator();
        configuration = JsonUtil.fromJson("""
                {
                    "server": "http://localhost:8080",
                    "username": "admin",
                    "password": "admin",
                    "realmName": "test-realm",
                    "configDirectory": "/path/to/config",
                    "clientIds": "",
                    "outputDirectory": "/path/to/output"
                }
                """, ExportSecretsCommandConfiguration.class);
    }

    @Nested
    class TemplateExportTests {
        @Test
        void shouldNotGenerateFiles_NoClientsGiven() {
            final Map<String, String> secretFiles = new HashMap<>();
            sut.generateSecretFiles(configuration, null, Collections.emptyMap(),
                    Collections.emptyMap(),
                    secretFiles::put);

            Assertions.assertTrue(secretFiles.isEmpty());
        }

        @Test
        void shouldNotGenerateFiles_NoTemplatesGiven() {
            final Map<String, ClientRepresentation> clients = new HashMap<>();
            clients.put("client1", new ClientRepresentation());

            final Map<String, String> secretFiles = new HashMap<>();
            sut.generateSecretFiles(configuration, null, Collections.emptyMap(), clients,
                    secretFiles::put);

            Assertions.assertTrue(secretFiles.isEmpty());
        }

        @Test
        void shouldGenerateFourFiles_TwoTemplatesGiven_TwoClientsGiven() throws ParseException {
            final Map<String, ClientRepresentation> clients = new HashMap<>();
            clients.put("client1", createClient("client1", "secret"));
            clients.put("client2", createClient("client2", "my-secret"));
            final Collection<Template> templates = new ArrayList<>();
            templates.add(createTemplate("client_id.env", "CLIENT_ID=$client_id"));
            templates.add(createTemplate("client_id.secret", "SECRET=$secret"));

            final Map<String, String> secretFiles = new HashMap<>();
            sut.generateSecretFiles(configuration, templates, Collections.emptyMap(), clients,
                    secretFiles::put);

            Assertions.assertEquals(4, secretFiles.size());
            Assertions.assertTrue(secretFiles.containsKey("client1.env"));
            Assertions.assertTrue(secretFiles.containsKey("client1.secret"));
            Assertions.assertTrue(secretFiles.containsKey("client2.env"));
            Assertions.assertTrue(secretFiles.containsKey("client2.secret"));
            Assertions.assertEquals("CLIENT_ID=client1", secretFiles.get("client1.env"));
            Assertions.assertEquals("SECRET=secret", secretFiles.get("client1.secret"));
            Assertions.assertEquals("CLIENT_ID=client2", secretFiles.get("client2.env"));
            Assertions.assertEquals("SECRET=my-secret", secretFiles.get("client2.secret"));
        }

        @Test
        void shouldGenerateFourFiles_ThreeTemplatesGiven_TwoClientsGiven_OneSpecificTemplate()
                throws ParseException {
            final Map<String, ClientRepresentation> clients = new HashMap<>();
            clients.put("client1", createClient("client1", "secret"));
            clients.put("client2", createClient("client2", "my-secret"));
            final Collection<Template> templates = new ArrayList<>();
            templates.add(createTemplate("client_id.env", "CLIENT_ID=$client_id"));
            templates.add(createTemplate("client_id.secret", "SECRET=$secret"));
            templates.add(createTemplate("client1.secret", "SPECIFIC_SECRET=$secret"));

            final Map<String, String> secretFiles = new HashMap<>();
            sut.generateSecretFiles(configuration, templates, Collections.emptyMap(), clients,
                    secretFiles::put);

            Assertions.assertEquals(4, secretFiles.size());
            Assertions.assertTrue(secretFiles.containsKey("client1.env"));
            Assertions.assertTrue(secretFiles.containsKey("client1.secret"));
            Assertions.assertTrue(secretFiles.containsKey("client2.env"));
            Assertions.assertTrue(secretFiles.containsKey("client2.secret"));
            Assertions.assertEquals("CLIENT_ID=client1", secretFiles.get("client1.env"));
            Assertions.assertEquals("SPECIFIC_SECRET=secret", secretFiles.get("client1.secret"));
            Assertions.assertEquals("CLIENT_ID=client2", secretFiles.get("client2.env"));
            Assertions.assertEquals("SECRET=my-secret", secretFiles.get("client2.secret"));
        }

        @Test
        void shouldUseCorrectClientForExport_ClientNamingIsOverlapping() throws ParseException {
            final Map<String, ClientRepresentation> clients = new LinkedHashMap<>();
            clients.put("eam-ui", createClient("eam-ui", "eam-ui"));
            clients.put("eam", createClient("eam", "eam"));
            final Collection<Template> templates = new ArrayList<>();
            templates.add(createTemplate("eam-ui.secret", "SPECIFIC_SECRET=$secret"));
            templates.add(createTemplate("client_id.secret", "SECRET=$secret"));

            final Map<String, String> secretFiles = new HashMap<>();
            sut.generateSecretFiles(configuration, templates, Collections.emptyMap(), clients,
                    secretFiles::put);

            Assertions.assertEquals(2, secretFiles.size());
            Assertions.assertTrue(secretFiles.containsKey("eam.secret"));
            Assertions.assertTrue(secretFiles.containsKey("eam-ui.secret"));
            Assertions.assertEquals("SECRET=eam", secretFiles.get("eam.secret"));
            Assertions.assertEquals("SPECIFIC_SECRET=eam-ui", secretFiles.get("eam-ui.secret"));
        }

        @Test
        void shouldExportClientsBasedOnFilter() throws ParseException {
            final Map<String, ClientRepresentation> clients = new LinkedHashMap<>();
            clients.put("eam-ui", createClient("eam-ui", "eam-ui"));
            clients.put("eam", createClient("eam", "eam"));
            final Collection<Template> templates = new ArrayList<>();
            templates.add(createTemplate("client_id.secret", "SECRET=$secret"));

            configuration = JsonUtil.fromJson("""
                    {
                        "server": "http://localhost:8080",
                        "username": "admin",
                        "password": "admin",
                        "realmName": "test-realm",
                        "configDirectory": "/path/to/config",
                        "clientIds": "eam",
                        "outputDirectory": "/path/to/output"
                    }
                    """, ExportSecretsCommandConfiguration.class);

            final Map<String, String> secretFiles = new HashMap<>();
            sut.generateSecretFiles(configuration, templates, Collections.emptyMap(), clients,
                    secretFiles::put);

            Assertions.assertEquals(1, secretFiles.size());
            Assertions.assertTrue(secretFiles.containsKey("eam.secret"));
            Assertions.assertEquals("SECRET=eam", secretFiles.get("eam.secret"));
        }
    }

    @Nested
    class VariableInterpolationTests {
        @Test
        void shouldSupportClientDataInExport() throws ParseException {
            final Map<String, ClientRepresentation> clients = new LinkedHashMap<>();
            clients.put("eam", createClient("eam", "eam"));
            final Collection<Template> templates = new ArrayList<>();
            templates.add(createTemplate("client_id.secret", """
                    SECRET=$secret
                    CLIENT_ID=$client_id
                    CLIENT_NAME=$client.name
                    CLIENT_NAMES=$clients["eam"].name
                    REALM=$realm
                    AUTH_SERVER_URL=$auth_server_url
                    """));

            final Map<String, String> secretFiles = new HashMap<>();
            sut.generateSecretFiles(configuration, templates, Collections.emptyMap(), clients,
                    secretFiles::put);

            Assertions.assertEquals(1, secretFiles.size());
            Assertions.assertTrue(secretFiles.containsKey("eam.secret"));
            Assertions.assertEquals("""
                    SECRET=eam
                    CLIENT_ID=eam
                    CLIENT_NAME=eam
                    CLIENT_NAMES=eam
                    REALM=test-realm
                    AUTH_SERVER_URL=http://localhost:8080
                    """, secretFiles.get("eam.secret"));
        }

        @Test
        void shouldSupportEnvironmentVariablesInExport_KCCPrefixOnly() throws ParseException {
            final Map<String, ClientRepresentation> clients = new LinkedHashMap<>();
            clients.put("eam", createClient("eam", "eam"));
            final Collection<Template> templates = new ArrayList<>();
            templates.add(createTemplate("client_id.secret", """
                    DATASOURCE_JDBC_URL=jdbc:postgresql://$env.KCC_DATABASE_NAME:5432
                    """));
            final Map<String, String> environmentVariables = new HashMap<>();
            environmentVariables.put("KCC_DATABASE_NAME", "database");

            final Map<String, String> secretFiles = new HashMap<>();
            sut.generateSecretFiles(configuration, templates, environmentVariables, clients,
                    secretFiles::put);

            Assertions.assertEquals(1, secretFiles.size());
            Assertions.assertTrue(secretFiles.containsKey("eam.secret"));
            Assertions.assertEquals("""
                    DATASOURCE_JDBC_URL=jdbc:postgresql://database:5432
                    """, secretFiles.get("eam.secret"));
        }
    }

    private ClientRepresentation createClient(final String clientId, final String secret) {
        final ClientRepresentation client = new ClientRepresentation();
        client.setClientId(clientId);
        client.setSecret(secret);
        client.setName(clientId);
        return client;
    }

    private Template createTemplate(final String name, final String content) throws ParseException {
        return VelocityUtils.loadTemplateFromString(name, content);
    }
}
