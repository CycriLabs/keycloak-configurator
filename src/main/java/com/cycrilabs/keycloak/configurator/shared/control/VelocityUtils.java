package com.cycrilabs.keycloak.configurator.shared.control;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import lombok.NoArgsConstructor;

import org.apache.commons.io.FileUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.RuntimeSingleton;
import org.apache.velocity.runtime.parser.ParseException;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class VelocityUtils {
    public static Collection<Template> loadTemplates(final Collection<File> templateFiles)
            throws IOException, ParseException {
        final Collection<Template> templates = new ArrayList<>(templateFiles.size());
        for (final File templateFile : templateFiles) {
            final Template template = VelocityUtils.loadTemplate(templateFile);
            templates.add(template);
        }
        return templates;
    }

    public static Template loadTemplate(final File file) throws ParseException, IOException {
        final String templateString = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
        return loadTemplateFromString(file.getName(), templateString);
    }

    public static Template loadTemplateFromString(final String name, final String templateString)
            throws ParseException {
        final RuntimeServices runtimeServices = RuntimeSingleton.getRuntimeServices();
        final StringReader reader = new StringReader(templateString);
        final Template template = new Template();
        template.setName(name);
        template.setRuntimeServices(runtimeServices);
        template.setData(runtimeServices.parse(reader, template));
        template.initDocument();
        return template;
    }

    public static VelocityContext createVelocityContext(final Map<String, String> data) {
        return data.entrySet()
                .stream()
                .reduce(new VelocityContext(), (c, e) -> {
                    c.put(e.getKey(), e.getValue());
                    return c;
                }, (c1, c2) -> c1);
    }

    public static String mergeTemplate(final Template template, final VelocityContext context) {
        final StringWriter writer = new StringWriter();
        template.merge(context, writer);
        return writer.toString();
    }
}
