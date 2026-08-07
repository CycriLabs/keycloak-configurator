package com.cycrilabs.keycloak.configurator.shared.control;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import org.apache.velocity.Template;
import org.apache.velocity.runtime.parser.ParseException;

import com.fasterxml.jackson.core.type.TypeReference;

/**
 * Loads entities from local configuration files. Configuration files are Velocity templates,
 * so the supported variables are expanded before the content is deserialized.
 * <p>
 * This is shared by all commands reading the configuration-as-code files, so that a comparison
 * against the server operates on exactly the same content that an import would apply.
 */
@ApplicationScoped
public class ConfigurationEntityLoader {
    private static final String VARIABLE_ENVIRONMENT = "env";

    @Inject
    EnvironmentVariableProvider environmentVariableProvider;

    private Map<String, String> environmentVariables;

    @PostConstruct
    public void init() {
        environmentVariables = environmentVariableProvider.load();
    }

    /**
     * Loads the entity of the given file and converts it to an object of the given class.
     *
     * @param filepath
     *         path to the configuration file
     * @param dtoClass
     *         class of the object to convert to
     * @param <T>
     *         type of the object to convert to
     * @return object of the given class
     */
    public <T> T loadEntity(final Path filepath, final Class<T> dtoClass) {
        return JsonUtil.fromJson(loadContent(filepath), dtoClass);
    }

    /**
     * Loads the entity of the given file and converts it to an object of the given type.
     *
     * @param filepath
     *         path to the configuration file
     * @param dtoType
     *         type of the object to convert to
     * @param <T>
     *         type of the object to convert to
     * @return object of the given type
     */
    public <T> T loadEntity(final Path filepath, final TypeReference<T> dtoType) {
        return JsonUtil.fromJson(loadContent(filepath), dtoType);
    }

    /**
     * Loads the content of the given file and expands all supported variables.
     *
     * @param filepath
     *         path to the configuration file
     * @return the expanded file content
     */
    public String loadContent(final Path filepath) {
        try {
            final Template template = VelocityUtils.loadTemplate(filepath.toFile());
            return VelocityUtils.mergeTemplate(template,
                    Map.ofEntries(Map.entry(VARIABLE_ENVIRONMENT, environmentVariables)));
        } catch (final IOException | ParseException e) {
            throw new IllegalStateException(e);
        }
    }
}
