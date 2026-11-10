package com.cycrilabs.keycloak.configurator.commands.configure.entity;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Scope mappings of a client (the roles allowed in its dedicated scope, relevant when
 * {@code fullScopeAllowed} is {@code false}). Uses the same shape as role composites:
 *
 * <pre>
 * {
 *     "realm": ["realm-role-a"],
 *     "client": {
 *         "realm-management": ["view-users", "manage-users"]
 *     }
 * }
 * </pre>
 */
@Getter
@Setter
@RegisterForReflection
public class ClientScopeMappingDTO {
    /** Realm roles to add to the client's scope. */
    private List<String> realm;
    /** Client roles to add to the client's scope, keyed by the client id of the role's client. */
    private Map<String, List<String>> client;
}
