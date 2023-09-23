package com.cycrilabs.keycloak.configurator.commands.configure.boundary;

import static io.quarkus.arc.ComponentsProvider.LOG;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

import org.keycloak.admin.client.Keycloak;

import com.cycrilabs.keycloak.configurator.commands.configure.control.EntityStore;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.ConfigureCommandConfiguration;
import com.cycrilabs.keycloak.configurator.commands.configure.entity.EntityImportType;
import com.cycrilabs.keycloak.configurator.shared.control.JsonbFactory;
import com.cycrilabs.keycloak.configurator.shared.control.KeycloakFactory;

import io.quarkus.logging.Log;

public abstract class AbstractImporter {
    public static final String PATH_SEPARATOR = Pattern.quote(System.getProperty("file.separator"));

    @Inject
    protected ConfigureCommandConfiguration configuration;
    protected Keycloak keycloak;
    protected EntityStore entityStore;

    @PostConstruct
    public void init() {
        keycloak = KeycloakFactory.create(configuration);
    }

    protected <T> T loadEntity(final Path filepath, final Class<T> dtoClass) {
        final String json = loadJsonFromResource(filepath);
        return fromJson(json, dtoClass);
    }

    private String loadJsonFromResource(final Path filePath) {
        try {
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            LOG.errorf("Could not read file {}", filePath);
            throw new RuntimeException(e);
        }
    }

    private <T> T fromJson(final String content, final Class<T> dtoClass) {
        return JsonbFactory.getJsonb().fromJson(content, dtoClass);
    }

    public void runImport(final EntityStore entityStore) {
        Log.infof("Executing importer %s.", getClass().getSimpleName());
        this.entityStore = entityStore;
        final List<Path> importFiles = getEntityFilePaths(getEntityDirectory());
        for (final Path importFile : importFiles) {
            final Object o = importFile(importFile);

        }
    }

    private List<Path> getEntityFilePaths(final String entityDir) {
        final String dir = Paths.get(configuration.getConfigDirectory(), entityDir).toString();
        try (final Stream<Path> stream = Files.walk(Paths.get(dir))) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".json"))
                    .toList();
        } catch (final IOException e) {
            Log.errorf("Could not read directory %s", dir);
            return Collections.emptyList();
        }
    }

    public int getPriority() {
        return getType().getPriority();
    }

    protected String getEntityDirectory() {
        return getType().getDirectory();
    }

    public abstract EntityImportType getType();

    protected abstract Object importFile(final Path file);
}
