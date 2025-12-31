package cc.sighs.auratip.util;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;

import java.util.HashMap;
import java.util.Map;

public final class SerializationUtil {
    public static Map<String, Dynamic<?>> convertMapToDynamic(Map<String, Object> source) {
        Map<String, Dynamic<?>> result = new HashMap<>();
        if (source == null || source.isEmpty()) return result;

        Gson gson = new Gson();
        source.forEach((k, v) -> {
            var jsonElement = gson.toJsonTree(v);
            Dynamic<?> dynamic = new Dynamic<>(JsonOps.INSTANCE, jsonElement);
            result.put(k, dynamic);
        });
        return result;
    }

    public static Dynamic<?> dynamicOf(String value) {
        JsonElement element = value == null ? JsonNull.INSTANCE : new JsonPrimitive(value);
        return new Dynamic<>(JsonOps.INSTANCE, element);
    }

    public static Dynamic<?> dynamicOf(int value) {
        JsonElement element = new JsonPrimitive(value);
        return new Dynamic<>(JsonOps.INSTANCE, element);
    }

    public static double getDouble(Map<String, Dynamic<?>> params, String key, double fallback) {
        Dynamic<?> dynamic = params.get(key);
        return dynamic == null ? fallback : dynamic.asDouble(fallback);
    }

    public static float getFloat(Map<String, Dynamic<?>> params, String key, float fallback) {
        Dynamic<?> d = params.get(key);
        return d == null ? fallback : d.asFloat(fallback);
    }

    public static int getInt(Map<String, Dynamic<?>> params, String key, int fallback) {
        Dynamic<?> d = params.get(key);
        return d == null ? fallback : d.asInt(fallback);
    }

    public static long getLong(Map<String, Dynamic<?>> params, String key, long fallback) {
        Dynamic<?> d = params.get(key);
        return d == null ? fallback : d.asLong(fallback);
    }

    public static boolean getBoolean(Map<String, Dynamic<?>> params, String key, boolean fallback) {
        Dynamic<?> d = params.get(key);
        return d == null ? fallback : d.asBoolean(fallback);
    }

    public static String getString(Map<String, Dynamic<?>> params, String key, String fallback) {
        Dynamic<?> d = params.get(key);
        return d == null ? fallback : d.asString(fallback);
    }

}
