package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;

import lombok.Getter;
import lombok.Setter;

import org.apache.velocity.Template;
import org.apache.velocity.runtime.parser.ParseException;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.ErrorRepresentation;

import com.cycrilabs.keycloak.configurator.commands.configure.control.ConfigurationFileStore;
import com.cycrilabs.keycloak.configurator.commands.configure.control.EntityStore;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigurationException;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigureCommandConfiguration;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ImporterStatus;
import com.cycrilabs.keycloak.configurator.shared.control.EnvironmentVariableProvider;
import com.cycrilabs.keycloak.configurator.shared.control.JsonUtil;
import com.cycrilabs.keycloak.configurator.shared.control.KeycloakFactory;
import com.cycrilabs.keycloak.configurator.shared.control.VelocityUtils;
import com.cycrilabs.keycloak.configurator.shared.entity.EntityType;
import com.fasterxml.jackson.core.type.TypeReference;

import io.quarkus.logging.Log;

public abstract class AbstractImporter {
    /**
     * The path separator for the current operating system. This is used to split the file path into
     * its parts. This is used in a regular expression, so special characters need to be escaped.
     */
    public static final String PATH_SEPARATOR =
            Pattern.quote(FileSystems.getDefault().getSeparator());

    private static final String VARIABLE_ENVIRONMENT = "env";

    @Inject
    protected ConfigureCommandConfiguration configuration;
    @Inject
    protected ConfigurationFileStore configurationFileStore;
    @Inject
    protected EntityStore entityStore;
    @Inject
    protected EnvironmentVariableProvider environmentVariableProvider;

    protected Keycloak keycloak;
    private Map<String, String> environmentVariables;
    @Getter
    @Setter
    private ImporterStatus status = ImporterStatus.NOT_STARTED;

    @PostConstruct
    public void init() {
        keycloak = KeycloakFactory.create(configuration);
        environmentVariables = environmentVariableProvider.load();
    }

    public void runImport() throws ConfigurationException {
        setStatus(ImporterStatus.STARTED);

        if (configuration.getEntityType() != null && configuration.getEntityType() != getType()) {
            Log.infof("Skipping importer '%s' for entity type '%s'.", getClass().getSimpleName(),
                    configuration.getEntityType());
            return;
        }

        Log.infof("Executing importer '%s'.", getClass().getSimpleName());
        for (final Path importFile : getImportFiles()) {
            try {
                Log.debugf("Importing file '%s'.", importFile);
                importFile(importFile);

                if (getStatus() == ImporterStatus.FAILURE && configuration.isExitOnError()) {
                    throw new ConfigurationException(
                            "Importer '%s' failed.".formatted(getClass().getSimpleName()));
                }
            } catch (final Exception e) {
                if (configuration.isExitOnError()) {
                    throw e;
                }
            }
        }

        setStatus(ImporterStatus.FINISHED);
    }

    private List<Path> getImportFiles() {
        final List<Path> importFiles = configurationFileStore.getImportFiles(getType());
        if (importFiles.isEmpty()) {
            Log.infof("No files found for importer '%s'.", getClass().getSimpleName());
        }
        return importFiles;
    }

    /**
     * Tries to extract Keycloak {@link ErrorRepresentation} from the exception.
     *
     * @param exception
     *         the thrown exception
     * @return the error representation based on the exception, or null otherwise
     */
    protected ErrorRepresentation extractError(final WebApplicationException exception) {
        return exception != null
               ? extractError(exception.getResponse())
               : null;
    }

    /**
     * Extracts a Keycloak {@link ErrorRepresentation} from the given Response.
     *
     * @param response
     *         the REST response
     * @return the error representation, or null otherwise
     */
    protected ErrorRepresentation extractError(final Response response) {
        return response != null
               ? response.readEntity(ErrorRepresentation.class)
               : null;
    }

    protected boolean isConflict(final Response response) {
        return response != null && response.getStatus() == Response.Status.CONFLICT.getStatusCode();
    }

    public int getPriority() {
        return getType().getPriority();
    }

    protected <T> T loadEntity(final Path filepath, final Class<T> dtoClass) {
        final String content = loadContent(filepath);
        return JsonUtil.fromJson(content, dtoClass);
    }

    protected <T> T loadEntity(final Path filepath, final TypeReference<T> dtoType) {
        final String content = loadContent(filepath);
        return JsonUtil.fromJson(content, dtoType);
    }

    private String loadContent(final Path filepath) {
        try {
            final Template template = VelocityUtils.loadTemplate(filepath.toFile());
            return VelocityUtils.mergeTemplate(template,
                    Map.ofEntries(Map.entry(VARIABLE_ENVIRONMENT, environmentVariables)));
        } catch (final IOException | ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    public abstract EntityType getType();

    protected abstract Object importFile(final Path file);
}
