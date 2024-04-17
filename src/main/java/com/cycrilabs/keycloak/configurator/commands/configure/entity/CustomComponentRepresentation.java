package com.cycrilabs.keycloak.configurator.commands.configure.entity;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.representations.idm.ComponentRepresentation;

/**
 * Custom representation of a Keycloak component that avoids MultivaluedHashMap for the config
 * property. This class can't be serialized/deserialized out-of-the-box by JSONB.
 */
@Getter
@Setter
public class CustomComponentRepresentation {

    private String id;
    private String name;
    private String providerId;
    private String providerType;
    private String parentId;
    private String subType;
    private Map<String, List<String>> config;

    public ComponentRepresentation toAPI() {
        final ComponentRepresentation componentRepresentation = new ComponentRepresentation();
        componentRepresentation.setId(id);
        componentRepresentation.setName(name);
        componentRepresentation.setProviderId(providerId);
        componentRepresentation.setProviderType(providerType);
        componentRepresentation.setParentId(parentId);
        componentRepresentation.setSubType(subType);
        componentRepresentation.setConfig(new MultivaluedHashMap<>(config));
        return componentRepresentation;
    }

    public static CustomComponentRepresentation fromAPI(
            final ComponentRepresentation componentRepresentation) {
        final CustomComponentRepresentation customComponentRepresentation =
                new CustomComponentRepresentation();
        customComponentRepresentation.setId(componentRepresentation.getId());
        customComponentRepresentation.setName(componentRepresentation.getName());
        customComponentRepresentation.setProviderId(componentRepresentation.getProviderId());
        customComponentRepresentation.setProviderType(componentRepresentation.getProviderType());
        customComponentRepresentation.setParentId(componentRepresentation.getParentId());
        customComponentRepresentation.setSubType(componentRepresentation.getSubType());
        customComponentRepresentation.setConfig(componentRepresentation.getConfig());
        return customComponentRepresentation;
    }

}
