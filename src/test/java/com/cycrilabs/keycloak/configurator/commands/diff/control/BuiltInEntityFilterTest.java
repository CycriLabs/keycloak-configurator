package com.cycrilabs.keycloak.configurator.commands.diff.control;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.cycrilabs.keycloak.configurator.commands.diff.entity.DiffCommandConfiguration;
import com.cycrilabs.keycloak.configurator.shared.control.JsonUtil;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

class BuiltInEntityFilterTest {
    private BuiltInEntityFilter sut;

    @BeforeEach
    void setUp() {
        sut = new BuiltInEntityFilter();
        sut.configuration = configuration(false, "[]");
    }

    @Nested
    class BuiltInTests {
        @Test
        void shouldIgnoreBuiltInClients() {
            Assertions.assertTrue(sut.isIgnored(EntityType.CLIENT, "realm-a", "account"));
            Assertions.assertTrue(sut.isIgnored(EntityType.CLIENT, "realm-a", "admin-cli"));
            Assertions.assertTrue(
                    sut.isIgnored(EntityType.CLIENT, "realm-a", "security-admin-console"));
            Assertions.assertFalse(sut.isIgnored(EntityType.CLIENT, "realm-a", "blog"));
        }

        @Test
        void shouldIgnoreRealmManagementClients_MasterRealmOnly() {
            // The master realm holds a management client for every other realm.
            Assertions.assertTrue(sut.isIgnored(EntityType.CLIENT, "master", "realm-a-realm"));
            Assertions.assertFalse(sut.isIgnored(EntityType.CLIENT, "realm-a", "realm-a-realm"));
        }

        @Test
        void shouldIgnoreDefaultRoleOfRealm() {
            Assertions.assertTrue(
                    sut.isIgnored(EntityType.REALM_ROLE, "realm-a", "default-roles-realm-a"));
            Assertions.assertTrue(
                    sut.isIgnored(EntityType.REALM_ROLE, "realm-a", "offline_access"));
            // The default role of a different realm is not a built-in of this realm.
            Assertions.assertFalse(
                    sut.isIgnored(EntityType.REALM_ROLE, "realm-a", "default-roles-realm-b"));
            Assertions.assertFalse(
                    sut.isIgnored(EntityType.REALM_ROLE, "realm-a", "blog-administration"));
        }

        @Test
        void shouldIgnoreAdminRoles_MasterRealmOnly() {
            Assertions.assertTrue(sut.isIgnored(EntityType.REALM_ROLE, "master", "create-realm"));
            Assertions.assertFalse(
                    sut.isIgnored(EntityType.REALM_ROLE, "realm-a", "create-realm"));
        }

        @Test
        void shouldIgnoreRolesOfBuiltInClients() {
            Assertions.assertTrue(
                    sut.isIgnored(EntityType.CLIENT_ROLE, "realm-a", "account:manage-account"));
            Assertions.assertFalse(
                    sut.isIgnored(EntityType.CLIENT_ROLE, "realm-a", "blog:posts-create"));
        }

        @Test
        void shouldIgnoreServiceAccountsAndTheAdminUser() {
            Assertions.assertTrue(
                    sut.isIgnored(EntityType.USER, "realm-a", "service-account-blog"));
            // The user the tool authenticated with is never part of the configuration.
            Assertions.assertTrue(sut.isIgnored(EntityType.USER, "realm-a", "keycloak"));
            Assertions.assertFalse(
                    sut.isIgnored(EntityType.USER, "realm-a", "alice@maildrop.cc"));
        }

        @Test
        void shouldIgnoreGeneratedKeyProvidersAndPolicies() {
            Assertions.assertTrue(
                    sut.isIgnored(EntityType.COMPONENT, "realm-a", "rsa-generated"));
            Assertions.assertTrue(
                    sut.isIgnored(EntityType.COMPONENT, "realm-a", "Trusted Hosts"));
            Assertions.assertFalse(
                    sut.isIgnored(EntityType.COMPONENT, "realm-a", "ldap-provider"));
        }

        @Test
        void shouldIgnoreBuiltInClientScopesAndMasterRealm() {
            Assertions.assertTrue(sut.isIgnored(EntityType.CLIENT_SCOPE, "realm-a", "profile"));
            Assertions.assertFalse(
                    sut.isIgnored(EntityType.CLIENT_SCOPE, "realm-a", "user-info-scope"));
            Assertions.assertTrue(sut.isIgnored(EntityType.REALM, "master", "master"));
            Assertions.assertFalse(sut.isIgnored(EntityType.REALM, "realm-a", "realm-a"));
        }

        @Test
        void shouldNotIgnoreGroups() {
            Assertions.assertFalse(sut.isIgnored(EntityType.GROUP, "realm-a", "/cycrilabs"));
        }
    }

    @Nested
    class AutoAssignedRoleTests {
        @Test
        void shouldTreatDefaultRoleOfRealmAsAutoAssigned() {
            Assertions.assertTrue(sut.isAutoAssignedRole("realm-a", "default-roles-realm-a"));
            Assertions.assertFalse(sut.isAutoAssignedRole("realm-a", "blog-administration"));
        }

        @Test
        void shouldNotTreatAnyRoleAsAutoAssigned_BuiltInsIncluded() {
            sut.configuration = configuration(true, "[]");

            Assertions.assertFalse(sut.isAutoAssignedRole("realm-a", "default-roles-realm-a"));
        }
    }

    @Nested
    class ConfigurationTests {
        @Test
        void shouldReportEverything_BuiltInsIncluded() {
            sut.configuration = configuration(true, "[]");

            Assertions.assertFalse(sut.isIgnored(EntityType.CLIENT, "realm-a", "account"));
            Assertions.assertFalse(sut.isIgnored(EntityType.REALM, "master", "master"));
        }

        @Test
        void shouldIgnoreExplicitlyConfiguredEntities() {
            sut.configuration =
                    configuration(false, "[ \"client:legacy-app\", \"realm-role:temp-admin\" ]");

            Assertions.assertTrue(sut.isIgnored(EntityType.CLIENT, "realm-a", "legacy-app"));
            Assertions.assertTrue(
                    sut.isIgnored(EntityType.REALM_ROLE, "realm-a", "temp-admin"));
            Assertions.assertFalse(sut.isIgnored(EntityType.CLIENT, "realm-a", "other-app"));
        }
    }

    private static DiffCommandConfiguration configuration(final boolean includeBuiltIns,
            final String ignoredExtras) {
        return JsonUtil.fromJson("""
                {
                    "server": "http://localhost:8080",
                    "username": "keycloak",
                    "password": "root",
                    "configDirectory": "./configuration",
                    "flatFiles": false,
                    "includeBuiltIns": %s,
                    "ignoredExtras": %s
                }
                """.formatted(Boolean.valueOf(includeBuiltIns), ignoredExtras),
                DiffCommandConfiguration.class);
    }
}
