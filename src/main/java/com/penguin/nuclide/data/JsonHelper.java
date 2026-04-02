package com.penguin.nuclide.data;

import java.util.Locale;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class JsonHelper {

    private JsonHelper() {}

    public static String requireString(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        if (element == null || element.isJsonNull()) {
            throw new IllegalStateException("Missing required string field: " + key);
        }
        return element.getAsString();
    }

    public static String optionalString(JsonObject obj, String key, String fallback) {
        JsonElement element = obj.get(key);
        if (element == null || element.isJsonNull()) {
            return fallback;
        }
        return element.getAsString();
    }

    public static double requireDouble(JsonObject obj, String key) {
        JsonElement element = obj.get(key);
        if (element == null || element.isJsonNull()) {
            throw new IllegalStateException("Missing required double field: " + key);
        }

        try {
            return element.getAsDouble();
        } catch (Exception e) {
            throw new IllegalStateException("Field '" + key + "' must be a double", e);
        }
    }

    public static double optionalDouble(JsonObject obj, String key, double fallback) {
        JsonElement element = obj.get(key);
        if (element == null || element.isJsonNull()) {
            return fallback;
        }

        try {
            return element.getAsDouble();
        } catch (Exception e) {
            throw new IllegalStateException("Field '" + key + "' must be a double", e);
        }
    }

    public static boolean optionalBoolean(JsonObject obj, String key, boolean fallback) {
        JsonElement element = obj.get(key);
        if (element == null || element.isJsonNull()) {
            return fallback;
        }

        try {
            return element.getAsBoolean();
        } catch (Exception e) {
            throw new IllegalStateException("Field '" + key + "' must be a boolean", e);
        }
    }

    public static <E extends Enum<E>> E optionalEnum(
            JsonObject obj,
            String key,
            Class<E> enumClass,
            E fallback
    ) {
        JsonElement element = obj.get(key);
        if (element == null || element.isJsonNull()) {
            return fallback;
        }

        String raw = element.getAsString();
        try {
            return Enum.valueOf(enumClass, raw.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException(
                    "Field '" + key + "' must be one of " + enumValues(enumClass) +
                    ", got '" + raw + "'",
                    e
            );
        }
    }

    private static <E extends Enum<E>> String enumValues(Class<E> enumClass) {
        StringBuilder sb = new StringBuilder();
        E[] values = enumClass.getEnumConstants();

        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(", ");
            sb.append(values[i].name().toLowerCase(Locale.ROOT));
        }

        return sb.toString();
    }
}