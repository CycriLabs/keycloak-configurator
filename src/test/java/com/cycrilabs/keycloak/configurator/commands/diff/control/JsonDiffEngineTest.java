package com.cycrilabs.keycloak.configurator.commands.diff.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cycrilabs.keycloak.configurator.commands.diff.entity.FieldDifference;
import com.cycrilabs.keycloak.configurator.commands.diff.entity.FieldDifferenceKind;
import com.cycrilabs.keycloak.configurator.shared.control.JsonUtil;

class JsonDiffEngineTest {
    private static final Set<String> GENERATED_IDENTIFIERS = Set.of("id", "containerId");

    @Nested
    class DeclaredFieldTests {
        @Test
        void shouldOnlyCompareLocallyDeclaredFields() {
            final List<FieldDifference> differences = compare("""
                    {
                        "clientId": "blog",
                        "enabled": true
                    }
                    """, """
                    {
                        "clientId": "blog",
                        "enabled": false,
                        "id": "6f1a",
                        "secret": "server-secret",
                        "nodeReRegistrationTimeout": -1
                    }
                    """);

            assertSingleDifference(differences, "enabled", FieldDifferenceKind.VALUE_DIFFERENT,
                    "true", "false");
        }

        @Test
        void shouldReportField_NotSetOnServer() {
            final List<FieldDifference> differences = compare("""
                    {
                        "description": "Create posts"
                    }
                    """, "{ }");

            assertSingleDifference(differences, "description",
                    FieldDifferenceKind.MISSING_ON_SERVER, "\"Create posts\"", null);
        }

        @Test
        void shouldNotReportDifference_FieldIsNullLocally() {
            // A configuration file exported from a server contains many null fields. They declare
            // nothing, so there is nothing to compare.
            final List<FieldDifference> differences = compare("""
                    {
                        "description": null
                    }
                    """, "{ }");

            Assertions.assertTrue(differences.isEmpty());
        }

        @Test
        void shouldCompareNestedObjects() {
            final List<FieldDifference> differences = compare("""
                    {
                        "attributes": { "a": "1" }
                    }
                    """, """
                    {
                        "attributes": { "a": "2", "b": "3" }
                    }
                    """);

            assertSingleDifference(differences, "attributes.a",
                    FieldDifferenceKind.VALUE_DIFFERENT, "\"1\"", "\"2\"");
        }

        @Test
        void shouldReportDifference_TypeOfFieldChanged() {
            final List<FieldDifference> differences = compare("""
                    {
                        "attributes": { "a": "1" }
                    }
                    """, """
                    {
                        "attributes": "not-an-object"
                    }
                    """);

            assertSingleDifference(differences, "attributes",
                    FieldDifferenceKind.VALUE_DIFFERENT, "{\"a\":\"1\"}", "\"not-an-object\"");
        }
    }

    @Nested
    class CollectionTests {
        @Test
        void shouldCompareCollectionOfValuesAsSet_BothDirections() {
            final List<FieldDifference> differences = compare("""
                    {
                        "redirectUris": [ "https://a", "https://b" ]
                    }
                    """, """
                    {
                        "redirectUris": [ "https://b", "https://c" ]
                    }
                    """);

            Assertions.assertEquals(2, differences.size());
            assertContains(differences, "redirectUris", FieldDifferenceKind.ONLY_IN_LOCAL,
                    "[https://a]", null);
            assertContains(differences, "redirectUris", FieldDifferenceKind.ONLY_ON_SERVER, null,
                    "[https://c]");
        }

        @Test
        void shouldNotReportDifference_OnlyOrderOfCollectionDiffers() {
            final List<FieldDifference> differences = compare("""
                    {
                        "redirectUris": [ "https://a", "https://b" ]
                    }
                    """, """
                    {
                        "redirectUris": [ "https://b", "https://a" ]
                    }
                    """);

            Assertions.assertTrue(differences.isEmpty());
        }

        @Test
        void shouldReportDifference_DeclaredCollectionIsEmptyLocally() {
            final List<FieldDifference> differences = compare("""
                    {
                        "redirectUris": []
                    }
                    """, """
                    {
                        "redirectUris": [ "https://a" ]
                    }
                    """);

            assertSingleDifference(differences, "redirectUris",
                    FieldDifferenceKind.ONLY_ON_SERVER, null, "[https://a]");
        }

        @Test
        void shouldMatchCollectionOfObjectsByName() {
            final List<FieldDifference> differences = compare("""
                    {
                        "protocolMappers": [
                            { "name": "audience", "consentRequired": false }
                        ]
                    }
                    """, """
                    {
                        "protocolMappers": [
                            { "name": "audience", "consentRequired": true, "id": "6f1a" },
                            { "name": "locale" }
                        ]
                    }
                    """);

            Assertions.assertEquals(2, differences.size());
            assertContains(differences, "protocolMappers[audience].consentRequired",
                    FieldDifferenceKind.VALUE_DIFFERENT, "false", "true");
            assertContains(differences, "protocolMappers", FieldDifferenceKind.ONLY_ON_SERVER, null,
                    "[locale]");
        }
    }

    @Nested
    class ValueComparisonTests {
        @Test
        void shouldNotReportDifference_NumbersOfDifferentJsonTypeHoldSameValue() {
            final List<FieldDifference> differences = compare("""
                    {
                        "notBefore": 1,
                        "accessTokenLifespan": 300
                    }
                    """, """
                    {
                        "notBefore": 1.0,
                        "accessTokenLifespan": 300
                    }
                    """);

            Assertions.assertTrue(differences.isEmpty());
        }

        @Test
        void shouldNotReportDifference_BooleanIsWrittenAsText() {
            final List<FieldDifference> differences = compare("""
                    {
                        "enabled": "true"
                    }
                    """, """
                    {
                        "enabled": true
                    }
                    """);

            Assertions.assertTrue(differences.isEmpty());
        }
    }

    @Nested
    class IgnoredFieldTests {
        @Test
        void shouldIgnoreGeneratedIdentifiers_AtAnyDepth() {
            final List<FieldDifference> differences = compare("""
                    {
                        "id": "local-id",
                        "protocolMappers": [ { "name": "audience", "id": "local-mapper-id" } ]
                    }
                    """, """
                    {
                        "id": "server-id",
                        "protocolMappers": [ { "name": "audience", "id": "server-mapper-id" } ]
                    }
                    """);

            Assertions.assertTrue(differences.isEmpty());
        }

        @Test
        void shouldIgnoreExactPathOnly_FullPathGiven() {
            final List<FieldDifference> differences = compare("""
                    {
                        "config": { "bindDn": "cn=local" },
                        "bindDn": "cn=local-root"
                    }
                    """, """
                    {
                        "config": { "bindDn": "cn=server" },
                        "bindDn": "cn=server-root"
                    }
                    """, Set.of("config.bindDn"));

            assertSingleDifference(differences, "bindDn", FieldDifferenceKind.VALUE_DIFFERENT,
                    "\"cn=local-root\"", "\"cn=server-root\"");
        }
    }

    @Nested
    class RedactionTests {
        @Test
        void shouldRedactSensitiveValues_ButStillReportDifference() {
            final List<FieldDifference> differences = compare("""
                    {
                        "secret": "local-secret",
                        "config": { "bindCredential": [ "local-password" ] }
                    }
                    """, """
                    {
                        "secret": "server-secret",
                        "config": { "bindCredential": [ "server-password" ] }
                    }
                    """);

            Assertions.assertEquals(3, differences.size());
            assertContains(differences, "secret", FieldDifferenceKind.VALUE_DIFFERENT, "<redacted>",
                    "<redacted>");
            assertContains(differences, "config.bindCredential",
                    FieldDifferenceKind.ONLY_IN_LOCAL, "<redacted>", null);
            assertContains(differences, "config.bindCredential",
                    FieldDifferenceKind.ONLY_ON_SERVER, null, "<redacted>");
            differences.forEach(difference -> {
                Assertions.assertFalse(String.valueOf(difference.localValue())
                        .contains("local-password"));
                Assertions.assertFalse(String.valueOf(difference.serverValue())
                        .contains("server-password"));
            });
        }

        @Test
        void shouldRedactSensitiveValues_NestedInRenderedObject() {
            // The whole credential is rendered because it has no counterpart on the server.
            final List<FieldDifference> differences = compare("""
                    {
                        "credentials": [ { "type": "password", "value": "hunter2" } ]
                    }
                    """, """
                    {
                        "credentials": []
                    }
                    """);

            assertSingleDifference(differences, "credentials", FieldDifferenceKind.ONLY_IN_LOCAL,
                    "{\"type\":\"password\",\"value\":\"<redacted>\"}", null);
        }
    }

    private static List<FieldDifference> compare(final String local, final String server) {
        return compare(local, server, GENERATED_IDENTIFIERS);
    }

    private static List<FieldDifference> compare(final String local, final String server,
            final Set<String> ignoredPaths) {
        final List<FieldDifference> differences = new ArrayList<>();
        JsonDiffEngine.compare(JsonUtil.readTree(local), JsonUtil.readTree(server), ignoredPaths,
                differences::add);
        return differences;
    }

    private static void assertSingleDifference(final List<FieldDifference> differences,
            final String path, final FieldDifferenceKind kind, final String localValue,
            final String serverValue) {
        Assertions.assertEquals(1, differences.size(), () -> "Expected one difference but got "
                + differences);
        assertContains(differences, path, kind, localValue, serverValue);
    }

    private static void assertContains(final List<FieldDifference> differences, final String path,
            final FieldDifferenceKind kind, final String localValue, final String serverValue) {
        final FieldDifference expected =
                new FieldDifference(path, kind, localValue, serverValue);
        Assertions.assertTrue(differences.contains(expected),
                () -> "Expected " + expected + " in " + differences);
    }
}
