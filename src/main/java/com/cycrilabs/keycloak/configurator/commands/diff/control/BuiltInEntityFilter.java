package com.cycrilabs.keycloak.configurator.commands.diff.control;

import java.util.Set;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import com.cycrilabs.keycloak.configurator.commands.diff.entity.DiffCommandConfiguration;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;

/**
 * Decides whether an entity that exists on the server but is not configured locally is a
 * Keycloak built-in or auto-generated entity.
 * <p>
 * Every realm ships with a set of clients, client scopes, roles and key providers that Keycloak
 * creates itself. Reporting them as findings would bury the actual configuration drift, so they
 * are filtered out by default. The filter can be disabled with {@code --include-built-ins} and
 * extended with {@code --ignore-extra}.
 */
@ApplicationScoped
public class BuiltInEntityFilter {
    private static final String REALM_MASTER = "master";
    private static final String IGNORE_SEPARATOR = ":";
    private static final String SUFFIX_REALM_CLIENT = "-realm";
    private static final String PREFIX_SERVICE_ACCOUNT = "service-account-";

    /**
     * Clients that Keycloak creates for every realm.
     */
    private static final Set<String> BUILT_IN_CLIENTS = Set.of(
            "account",
            "account-console",
            "admin-cli",
            "broker",
            "realm-management",
            "security-admin-console");

    /**
     * Client scopes that Keycloak creates for every realm.
     */
    private static final Set<String> BUILT_IN_CLIENT_SCOPES = Set.of(
            "acr",
            "address",
            "basic",
            "email",
            "microprofile-jwt",
            "offline_access",
            "organization",
            "phone",
            "profile",
            "role_list",
            "roles",
            "saml_organization",
            "service_account",
            "web-origins");

    /**
     * Realm roles that Keycloak creates for every realm. The composite default role is named
     * after the realm and therefore resolved dynamically.
     */
    private static final Set<String> BUILT_IN_REALM_ROLES = Set.of(
            "offline_access",
            "uma_authorization");

    /**
     * Realm roles that only exist in the master realm.
     */
    private static final Set<String> BUILT_IN_MASTER_REALM_ROLES = Set.of(
            "admin",
            "create-realm");

    /**
     * Key providers and client registration policies that Keycloak creates for every realm.
     */
    private static final Set<String> BUILT_IN_COMPONENTS = Set.of(
            "aes-generated",
            "hmac-generated",
            "hmac-generated-hs512",
            "rsa-generated",
            "rsa-enc-generated",
            "Allowed Client Scopes",
            "Allowed Protocol Mapper Types",
            "Allowed Registration Web Origins",
            "Consent Required",
            "Full Scope Disabled",
            "Max Clients Limit",
            "Trusted Hosts");

    /**
     * Component provider types that Keycloak manages itself. Every realm is created with a set of
     * key providers and client registration policies, and their names differ between Keycloak
     * versions. Matching on the provider type therefore covers them independently of the version.
     */
    private static final Set<String> BUILT_IN_COMPONENT_PROVIDER_TYPES = Set.of(
            "org.keycloak.keys.KeyProvider",
            "org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy");

    @Inject
    DiffCommandConfiguration configuration;

    /**
     * Whether an entity found on the server should not be reported as a missing local
     * configuration.
     *
     * @param entityType
     *         the type of the entity
     * @param realm
     *         the realm the entity belongs to
     * @param name
     *         the identifying name of the entity. For client roles this is expected as
     *         {@code clientId:roleName}
     * @return true if the entity is a built-in or explicitly ignored entity
     */
    public boolean isIgnored(final EntityType entityType, final String realm, final String name) {
        if (configuration.isIncludeBuiltIns()) {
            return false;
        }
        return isExplicitlyIgnored(entityType, name) || isBuiltIn(entityType, realm, name);
    }

    private boolean isExplicitlyIgnored(final EntityType entityType, final String name) {
        final String ignoreKey = entityType.getName() + IGNORE_SEPARATOR + name;
        return configuration.getIgnoredExtras().contains(ignoreKey);
    }

    private boolean isBuiltIn(final EntityType entityType, final String realm, final String name) {
        return switch (entityType) {
            case REALM -> REALM_MASTER.equals(name);
            case CLIENT -> isBuiltInClient(realm, name);
            case CLIENT_SCOPE -> BUILT_IN_CLIENT_SCOPES.contains(name);
            case CLIENT_ROLE -> isRoleOfBuiltInClient(realm, name);
            case REALM_ROLE -> isBuiltInRealmRole(realm, name);
            case USER -> isBuiltInUser(name);
            case COMPONENT -> BUILT_IN_COMPONENTS.contains(name);
            case GROUP, SERVICE_ACCOUNT_CLIENT_ROLE, SERVICE_ACCOUNT_REALM_ROLE -> false;
        };
    }

    private boolean isBuiltInClient(final String realm, final String clientId) {
        if (BUILT_IN_CLIENTS.contains(clientId)) {
            return true;
        }
        // The master realm holds a management client for every other realm, e.g. 'realm-a-realm'.
        return REALM_MASTER.equals(realm) && clientId.endsWith(SUFFIX_REALM_CLIENT);
    }

    /**
     * Roles of a built-in client are built-in as well, so they are ignored together with their
     * client.
     */
    private boolean isRoleOfBuiltInClient(final String realm, final String name) {
        final int separator = name.indexOf(IGNORE_SEPARATOR);
        if (separator < 0) {
            return false;
        }
        return isBuiltInClient(realm, name.substring(0, separator));
    }

    private boolean isBuiltInRealmRole(final String realm, final String name) {
        if (BUILT_IN_REALM_ROLES.contains(name) || defaultRolesName(realm).equals(name)) {
            return true;
        }
        return REALM_MASTER.equals(realm) && BUILT_IN_MASTER_REALM_ROLES.contains(name);
    }

    /**
     * Service account users are created together with their client, and the user the tool
     * authenticated with is never part of the configuration.
     */
    private boolean isBuiltInUser(final String username) {
        return username.startsWith(PREFIX_SERVICE_ACCOUNT)
                || username.equalsIgnoreCase(configuration.getUsername());
    }

    /**
     * Whether a component is managed by Keycloak itself, judged by its provider type.
     *
     * @param providerType
     *         the provider type of the component
     * @return true if Keycloak creates components of this type on its own
     */
    public boolean isBuiltInComponentProviderType(final String providerType) {
        return !configuration.isIncludeBuiltIns()
                && BUILT_IN_COMPONENT_PROVIDER_TYPES.contains(providerType);
    }

    /**
     * Whether a role is assigned by Keycloak itself. Keycloak grants the composite default role
     * of a realm to every new user and service account, so it shows up in every role mapping
     * read from the server without ever being configured locally.
     *
     * @param realm
     *         the realm the role belongs to
     * @param roleName
     *         the name of the role
     * @return true if the role assignment is created by Keycloak
     */
    public boolean isAutoAssignedRole(final String realm, final String roleName) {
        return !configuration.isIncludeBuiltIns() && defaultRolesName(realm).equals(roleName);
    }

    /**
     * The name of the composite default role Keycloak creates for a realm.
     *
     * @param realm
     *         the realm name
     * @return the name of the realm's default role
     */
    public static String defaultRolesName(final String realm) {
        return "default-roles-" + realm;
    }
}
