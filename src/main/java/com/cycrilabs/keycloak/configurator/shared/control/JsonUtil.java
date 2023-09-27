package com.cycrilabs.keycloak.configurator.shared.control;

import lombok.NoArgsConstructor;

/**
 * Helper class to convert JSON to/from objects. It uses the {@link JsonbFactory} to create a JSON-B
 * instance and the configured {@link jakarta.json.bind.Jsonb}.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class JsonUtil {
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
        return JsonbFactory.getJsonb().fromJson(content, dtoClass);
    }

    /**
     * Converts the given object to a JSON string.
     *
     * @param entity
     *         object to convert
     * @return JSON string
     */
    public static String toJson(final Object entity) {
        return JsonbFactory.getJsonb(true).toJson(entity);
    }
}
