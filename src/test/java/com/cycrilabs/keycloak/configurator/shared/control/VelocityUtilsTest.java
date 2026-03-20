package com.cycrilabs.keycloak.configurator.shared.control;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.apache.velocity.Template;
import org.apache.velocity.runtime.parser.ParseException;
import org.junit.jupiter.api.Test;

class VelocityUtilsTest {
    @Test
    void testParsingTemplateWithHyphen() throws ParseException {
        final String templateString = """
                {
                    "clientRole": false,
                    "composite": true,
                    "containerId": "default",
                    "description": "${role_default-roles}",
                    "name": "default-roles-default"
                }
                """;

        Map<String, Object> parameters = Map.of("role_default-roles", "test");
        Template template = VelocityUtils.loadTemplateFromString("test", templateString);
        String expandedString = VelocityUtils.mergeTemplate(template, parameters);
        assertTrue(expandedString.contains("\"description\": \"test\","));
    }

    @Test
    void testParsingTemplateWithHyphenWithoutParameter() throws ParseException {
        final String templateString = """
                {
                    "clientRole": false,
                    "composite": true,
                    "containerId": "default",
                    "description": "${role_default-roles}",
                    "name": "default-roles-default"
                }
                """;

        Map<String, Object> parameters = Map.of();
        Template template = VelocityUtils.loadTemplateFromString("test", templateString);
        String expandedString = VelocityUtils.mergeTemplate(template, parameters);
        assertTrue(expandedString.contains("\"description\": \"${role_default-roles}\","));
    }
}
