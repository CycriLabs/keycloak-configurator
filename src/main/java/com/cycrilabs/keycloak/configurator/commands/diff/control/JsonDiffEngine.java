package com.cycrilabs.keycloak.configurator.commands.diff.control;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import lombok.NoArgsConstructor;

import com.cycrilabs.keycloak.configurator.commands.diff.entity.FieldDifference;
import com.cycrilabs.keycloak.configurator.commands.diff.entity.FieldDifferenceKind;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Compares a locally configured entity against its server counterpart.
 * <p>
 * The comparison is driven by the <em>local</em> side: only fields that the local configuration
 * actually declares are compared. Fields that only the server knows about - such as generated
 * identifiers, secrets or Keycloak defaults - are not differences, because a configuration file
 * is intentionally a partial description of an entity.
 * <p>
 * Within a declared collection the comparison is bidirectional: since the field itself was
 * declared, elements that only exist on the server are reported as well. Collections of scalars
 * are compared as sets, because Keycloak does not treat their order as significant.
 */
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class JsonDiffEngine {
    /**
     * Field names whose values must never be written to the report.
     */
    private static final Set<String> REDACTED_FIELD_NAMES =
            Set.of("secret", "bindCredential", "password", "privateKey");
    private static final String REDACTED_PLACEHOLDER = "<redacted>";
    private static final String MATCH_FIELD_NAME = "name";
    private static final String FIELD_CREDENTIALS = "credentials";
    private static final String FIELD_VALUE = "value";

    /**
     * Compares the local entity against the server entity and reports every difference of a
     * locally declared field to the given sink.
     *
     * @param local
     *         the entity as declared in the local configuration
     * @param server
     *         the entity as returned by the server
     * @param ignoredPaths
     *         field paths that must not be compared because they are not comparable, e.g.
     *         because the local configuration references a parent by name while the server
     *         reports a generated identifier
     * @param sink
     *         receives every detected difference
     */
    public static void compare(final JsonNode local, final JsonNode server,
            final Set<String> ignoredPaths, final Consumer<FieldDifference> sink) {
        compareNode("", local, server, ignoredPaths, sink);
    }

    private static void compareNode(final String path, final JsonNode local, final JsonNode server,
            final Set<String> ignoredPaths, final Consumer<FieldDifference> sink) {
        if (local.isObject()) {
            compareObject(path, local, server, ignoredPaths, sink);
        } else if (local.isArray()) {
            compareArray(path, local, server, ignoredPaths, sink);
        } else {
            compareValue(path, local, server, sink);
        }
    }

    private static void compareObject(final String path, final JsonNode local,
            final JsonNode server, final Set<String> ignoredPaths,
            final Consumer<FieldDifference> sink) {
        if (!server.isObject()) {
            sink.accept(valueDifference(path, local, server));
            return;
        }

        for (final Map.Entry<String, JsonNode> field : local.properties()) {
            final String childPath = childPath(path, field.getKey());
            final JsonNode localChild = field.getValue();

            // A field that is absent or null locally declares nothing, so there is nothing to
            // compare. Configuration files exported from a server contain many null fields.
            if (isUndeclared(localChild) || isIgnored(childPath, ignoredPaths)) {
                continue;
            }

            final JsonNode serverChild = server.get(field.getKey());
            if (serverChild == null || serverChild.isNull()) {
                // An empty collection or object declares nothing, so it does not differ from a
                // field the server does not report at all.
                if (!isEmptyContainer(localChild)) {
                    sink.accept(new FieldDifference(childPath,
                            FieldDifferenceKind.MISSING_ON_SERVER, render(childPath, localChild),
                            null));
                }
                continue;
            }

            compareNode(childPath, localChild, serverChild, ignoredPaths, sink);
        }
    }

    private static void compareArray(final String path, final JsonNode local, final JsonNode server,
            final Set<String> ignoredPaths, final Consumer<FieldDifference> sink) {
        if (!server.isArray()) {
            sink.accept(valueDifference(path, local, server));
            return;
        }

        if (containsOnlyValues(local) && containsOnlyValues(server)) {
            compareValueArray(path, local, server, sink);
        } else if (isMatchableByName(local) && isMatchableByName(server)) {
            compareArrayByName(path, local, server, ignoredPaths, sink);
        } else {
            compareArrayByIndex(path, local, server, ignoredPaths, sink);
        }
    }

    /**
     * Compares collections of scalars as sets, as their order is not significant.
     */
    private static void compareValueArray(final String path, final JsonNode local,
            final JsonNode server, final Consumer<FieldDifference> sink) {
        final Set<String> localValues = toValueSet(local);
        final Set<String> serverValues = toValueSet(server);

        reportMissingElements(path, localValues, serverValues, FieldDifferenceKind.ONLY_IN_LOCAL,
                sink);
        reportMissingElements(path, serverValues, localValues, FieldDifferenceKind.ONLY_ON_SERVER,
                sink);
    }

    private static void reportMissingElements(final String path, final Set<String> from,
            final Set<String> other, final FieldDifferenceKind kind,
            final Consumer<FieldDifference> sink) {
        final List<String> missing = from.stream()
                .filter(value -> !other.contains(value))
                .toList();
        if (missing.isEmpty()) {
            return;
        }

        final String rendered = renderElements(path, missing);
        sink.accept(kind == FieldDifferenceKind.ONLY_IN_LOCAL
                    ? new FieldDifference(path, kind, rendered, null)
                    : new FieldDifference(path, kind, null, rendered));
    }

    /**
     * Compares collections of objects by their 'name' field, as their order is not significant.
     */
    private static void compareArrayByName(final String path, final JsonNode local,
            final JsonNode server, final Set<String> ignoredPaths,
            final Consumer<FieldDifference> sink) {
        final List<String> onlyInLocal = new ArrayList<>();
        for (final JsonNode localElement : local) {
            final String name = localElement.get(MATCH_FIELD_NAME).asText();
            final JsonNode serverElement = findByName(server, name);
            if (serverElement == null) {
                onlyInLocal.add(name);
            } else {
                compareNode(elementPath(path, name), localElement, serverElement, ignoredPaths,
                        sink);
            }
        }
        if (!onlyInLocal.isEmpty()) {
            sink.accept(new FieldDifference(path, FieldDifferenceKind.ONLY_IN_LOCAL,
                    renderElements(path, onlyInLocal), null));
        }

        final List<String> onlyOnServer = new ArrayList<>();
        for (final JsonNode serverElement : server) {
            final String name = serverElement.get(MATCH_FIELD_NAME).asText();
            if (findByName(local, name) == null) {
                onlyOnServer.add(name);
            }
        }
        if (!onlyOnServer.isEmpty()) {
            sink.accept(new FieldDifference(path, FieldDifferenceKind.ONLY_ON_SERVER, null,
                    renderElements(path, onlyOnServer)));
        }
    }

    /**
     * Fallback for collections whose elements cannot be identified by name.
     */
    private static void compareArrayByIndex(final String path, final JsonNode local,
            final JsonNode server, final Set<String> ignoredPaths,
            final Consumer<FieldDifference> sink) {
        for (int i = 0; i < local.size(); i++) {
            if (i >= server.size()) {
                sink.accept(new FieldDifference(path, FieldDifferenceKind.ONLY_IN_LOCAL,
                        render(path, local.get(i)), null));
            } else {
                compareNode(elementPath(path, String.valueOf(i)), local.get(i), server.get(i),
                        ignoredPaths, sink);
            }
        }
        for (int i = local.size(); i < server.size(); i++) {
            sink.accept(new FieldDifference(path, FieldDifferenceKind.ONLY_ON_SERVER, null,
                    render(path, server.get(i))));
        }
    }

    private static void compareValue(final String path, final JsonNode local, final JsonNode server,
            final Consumer<FieldDifference> sink) {
        if (!isEqualValue(local, server)) {
            sink.accept(valueDifference(path, local, server));
        }
    }

    /**
     * Compares two scalars leniently: numbers are compared by their numeric value so that an
     * integer and a long holding the same number are equal, everything else is compared by its
     * textual representation.
     */
    private static boolean isEqualValue(final JsonNode local, final JsonNode server) {
        if (local.isNumber() && server.isNumber()) {
            return local.decimalValue().compareTo(server.decimalValue()) == 0;
        }
        if (local.isContainerNode() || server.isContainerNode()) {
            return local.equals(server);
        }
        return local.asText().equals(server.asText());
    }

    private static FieldDifference valueDifference(final String path, final JsonNode local,
            final JsonNode server) {
        return new FieldDifference(path, FieldDifferenceKind.VALUE_DIFFERENT,
                render(path, local), render(path, server));
    }

    private static JsonNode findByName(final JsonNode array, final String name) {
        for (final JsonNode element : array) {
            if (name.equals(element.get(MATCH_FIELD_NAME).asText())) {
                return element;
            }
        }
        return null;
    }

    private static boolean isUndeclared(final JsonNode node) {
        return node == null || node.isNull();
    }

    private static boolean isEmptyContainer(final JsonNode node) {
        return node.isContainerNode() && node.isEmpty();
    }

    /**
     * Whether a field must not be compared. An entry of the ignore set matches either the full
     * path of the field or its name at any depth, so that e.g. 'id' suppresses all generated
     * identifiers while 'config.bindCredential' only suppresses that single field.
     *
     * @param path
     *         the full path of the field
     * @param ignoredPaths
     *         the configured ignore set
     * @return true if the field must not be compared
     */
    private static boolean isIgnored(final String path, final Set<String> ignoredPaths) {
        return ignoredPaths.contains(path) || ignoredPaths.contains(leafName(path));
    }

    private static boolean containsOnlyValues(final JsonNode array) {
        for (final JsonNode element : array) {
            if (element.isContainerNode()) {
                return false;
            }
        }
        return true;
    }

    private static boolean isMatchableByName(final JsonNode array) {
        if (array.isEmpty()) {
            return false;
        }
        for (final JsonNode element : array) {
            if (!element.isObject() || !element.hasNonNull(MATCH_FIELD_NAME)) {
                return false;
            }
        }
        return true;
    }

    private static Set<String> toValueSet(final JsonNode array) {
        final Set<String> values = new LinkedHashSet<>();
        for (final JsonNode element : array) {
            values.add(element.asText());
        }
        return values;
    }

    private static String childPath(final String path, final String field) {
        return path.isEmpty()
               ? field
               : path + "." + field;
    }

    private static String elementPath(final String path, final String element) {
        return path + "[" + element + "]";
    }

    private static String renderElements(final String path, final List<String> elements) {
        if (isRedacted(path)) {
            return REDACTED_PLACEHOLDER;
        }
        return "[" + String.join(", ", elements) + "]";
    }

    private static String render(final String path, final JsonNode node) {
        if (isRedacted(path)) {
            return REDACTED_PLACEHOLDER;
        }
        if (node == null || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            return "\"" + node.asText() + "\"";
        }
        if (node.isContainerNode()) {
            // A whole object or collection is rendered when it has no counterpart at all. It may
            // carry sensitive values further down, e.g. the credentials of a user. The rendered
            // node can be an element of a collection, so the context is taken from the path.
            return maskSensitiveValues(node, path.contains(FIELD_CREDENTIALS)).toString();
        }
        return node.toString();
    }

    /**
     * Creates a copy of the given node in which all sensitive values are replaced by a
     * placeholder.
     *
     * @param node
     *         the node to mask
     * @param withinCredentials
     *         whether the node is part of a credentials collection, whose values are sensitive
     *         regardless of their field name
     * @return the masked copy of the node
     */
    private static JsonNode maskSensitiveValues(final JsonNode node,
            final boolean withinCredentials) {
        if (node.isObject()) {
            final ObjectNode masked = ((ObjectNode) node).objectNode();
            for (final Map.Entry<String, JsonNode> field : node.properties()) {
                final String name = field.getKey();
                if (REDACTED_FIELD_NAMES.contains(name)
                        || (withinCredentials && FIELD_VALUE.equals(name))) {
                    masked.put(name, REDACTED_PLACEHOLDER);
                } else {
                    masked.set(name, maskSensitiveValues(field.getValue(),
                            withinCredentials || FIELD_CREDENTIALS.equals(name)));
                }
            }
            return masked;
        }

        if (node.isArray()) {
            final ArrayNode masked = ((ArrayNode) node).arrayNode();
            for (final JsonNode element : node) {
                masked.add(maskSensitiveValues(element, withinCredentials));
            }
            return masked;
        }

        return node;
    }

    /**
     * Whether the value of the given path is sensitive. The report is written to the console and
     * ends up in build logs, so such values are replaced by a placeholder. The difference itself
     * is still reported.
     *
     * @param path
     *         the field path to check
     * @return true if the value must not be rendered
     */
    private static boolean isRedacted(final String path) {
        final String leaf = leafName(path);
        return REDACTED_FIELD_NAMES.contains(leaf)
                || ("value".equals(leaf) && path.contains("credentials"));
    }

    private static String leafName(final String path) {
        final int lastSeparator = path.lastIndexOf('.');
        final String leaf = lastSeparator < 0
                            ? path
                            : path.substring(lastSeparator + 1);
        final int elementStart = leaf.indexOf('[');
        return elementStart < 0
               ? leaf
               : leaf.substring(0, elementStart);
    }
}
