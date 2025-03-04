package com.cycrilabs.keycloak.configurator.shared.control;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import lombok.NoArgsConstructor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import io.quarkus.logging.Log;

/**
 * Helper class to convert JSON to/from objects. It uses the Jackson {@link ObjectMapper}.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class JsonUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .enable(SerializationFeature.INDENT_OUTPUT);

    /**
     * Converts the given object to a JSON string.
     *
     * @param entity
     *         object to convert
     * @return JSON string
     */
    public static String toJson(final Object entity) {
        try {
            return OBJECT_MAPPER.writeValueAsString(entity);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Converts the given JSON string to an object of the given class.
     *
     * @param content
     *         JSON string
     * @param dtoClass
     *         class of the object to convert to
     * @param <T>
     *         type of the object to convert to
     * @return object of the given class
     */
    public static <T> T fromJson(final String content, final Class<T> dtoClass) {
        try {
            return OBJECT_MAPPER.readValue(content, dtoClass);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Loads an entity from the given file path. The file is expected to be a JSON file.
     * The JSON is converted to an object of the given class.
     *
     * @param filepath
     *         path to the JSON file
     * @param dtoClass
     *         class of the object to convert to
     * @param <T>
     *         type of the object to convert to
     * @return object of the given class
     */
    public static <T> T loadEntity(final Path filepath, final Class<T> dtoClass) {
        final String json = loadJsonFromPath(filepath);
        return fromJson(json, dtoClass);
    }

    /**
     * Converts the given JSON string to an object of the given class.
     *
     * @param content
     *         JSON string
     * @param dtoType
     *         type of the object to convert to
     * @param <T>
     *         type of the object to convert to
     * @return object of the given class
     */
    public static <T> T fromJson(final String content, final TypeReference<T> dtoType) {
        try {
            return OBJECT_MAPPER.readValue(content, dtoType);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Loads an entity from the given file path. The file is expected to be a JSON file.
     * The JSON is converted to an object of the given type.
     *
     * @param filepath
     *         path to the JSON file
     * @param dtoType
     *         type of the object to convert to
     * @param <T>
     *         type of the object to convert to
     * @return object of the given type
     */
    public static <T> T loadEntity(final Path filepath, final TypeReference<T> dtoType) {
        final String json = loadJsonFromPath(filepath);
        return fromJson(json, dtoType);
    }

    /**
     * Loads a JSON file from the given file path.
     *
     * @param filePath
     *         path to the JSON file
     * @return JSON string
     */
    public static String loadJsonFromPath(final Path filePath) {
        try {
            return Files.readString(filePath, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            Log.errorf("Could not read file '%s'.", filePath);
            throw new IllegalStateException(e);
        }
    }
}
