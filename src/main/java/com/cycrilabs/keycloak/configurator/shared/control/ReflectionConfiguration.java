package com.cycrilabs.keycloak.configurator.shared.control;

import org.keycloak.representations.idm.ClientPoliciesRepresentation;
import org.keycloak.representations.idm.ClientProfilesRepresentation;
import org.keycloak.representations.idm.ErrorRepresentation;

import io.quarkus.runtime.annotations.RegisterForReflection;

/**
 * Register classes for reflection. This is needed for native image builds. Otherwise, an exception
 * is thrown during runtime when trying to create those classes during serialization &
 * deserialization, e.g. like this:
 * <code>
 * jakarta.json.bind.JsonbException: Unable to deserialize property 'parsedClientProfiles' because
 * of: Cannot create instance of a class: class
 * org.keycloak.representations.idm.ClientProfilesRepresentation, No default constructor found.
 * </code>
 * <p>
 * The same applies when a representation read from the server is serialized again, which the
 * 'diff' command does to compare it against the local configuration.
 */
@RegisterForReflection(targets = { ClientPoliciesRepresentation.class,
        ClientProfilesRepresentation.class, ErrorRepresentation.class })
public class ReflectionConfiguration {
}
