package com.scaffold4j.util;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

/**
 * Thin wrapper around Mustache for template-based code generation.
 */
public final class TemplateEngine {

    private static final MustacheFactory MF = new DefaultMustacheFactory();

    private TemplateEngine() {}

    /**
     * Render an inline Mustache template string with the given context.
     */
    public static String render(String template, Map<String, Object> context) {
        try {
            Mustache mustache = MF.compile(new StringReader(template), "inline");
            StringWriter writer = new StringWriter();
            mustache.execute(writer, context).flush();
            return writer.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to render template", e);
        }
    }

    /**
     * Convenience: render with a single key-value pair.
     */
    public static String render(String template, String key, Object value) {
        return render(template, Map.of(key, value));
    }

    /**
     * Build a fluent context map.
     */
    public static TemplateContext context() {
        return new TemplateContext();
    }

    public static class TemplateContext {
        private final Map<String, Object> map = new java.util.LinkedHashMap<>();

        public TemplateContext put(String key, Object value) {
            map.put(key, value);
            return this;
        }

        public Map<String, Object> build() {
            return map;
        }

        public String render(String template) {
            return TemplateEngine.render(template, map);
        }
    }
}
