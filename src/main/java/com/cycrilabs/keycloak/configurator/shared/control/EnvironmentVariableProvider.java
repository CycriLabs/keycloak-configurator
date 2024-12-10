package com.cycrilabs.keycloak.configurator.shared.control;

import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.eclipse.microprofile.config.Config;

@ApplicationScoped
public class EnvironmentVariableProvider {
    private static final String ENV_VAR_PREFIX = "kcc";

    private final Config config;

    @Inject
    public EnvironmentVariableProvider(final Config config) {
        this.config = config;
    }

    public Map<String, String> load() {
        return StreamSupport.stream(config.getPropertyNames().spliterator(), false)
                .filter(name -> name.toLowerCase().startsWith(ENV_VAR_PREFIX))
                .map(name -> Map.entry(name, config.getValue(name, String.class)))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
}
