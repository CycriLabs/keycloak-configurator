package com.cycrilabs.keycloak.configurator.shared.control;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

import lombok.NoArgsConstructor;

@NoArgsConstructor(staticName = "getInstance")
public class JsonbFactory {
    private static final JsonbFactory INSTANCE = new JsonbFactory();

    private Jsonb jsonbFormatting;
    private Jsonb jsonb;

    private JsonbConfig getConfig(final boolean formatting) {
        return new JsonbConfig()
                .withFormatting(Boolean.valueOf(formatting));
    }

    private Jsonb getConfiguredJsonb(final boolean formatting) {
        if (formatting) {
            return getConfiguredJsonbFormatting();
        }
        if (jsonb == null) {
            jsonb = JsonbBuilder.create(getConfig(false));
        }
        return jsonb;
    }

    private Jsonb getConfiguredJsonbFormatting() {
        if (jsonbFormatting == null) {
            jsonbFormatting = JsonbBuilder.create(getConfig(true));
        }
        return jsonbFormatting;
    }

    public static Jsonb getJsonb() {
        return getJsonb(false);
    }

    public static Jsonb getJsonb(final boolean formatting) {
        return INSTANCE.getConfiguredJsonb(formatting);
    }
}
