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
    private SerializationUtil() {
    }

    private static final ThreadLocal<ParamCapture> PARAM_CAPTURE = new ThreadLocal<>();
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
        captureParam("number", key, fallback);
        Dynamic<?> dynamic = params.get(key);
        return dynamic == null ? fallback : dynamic.asDouble(fallback);
    }

    public static float getFloat(Map<String, Dynamic<?>> params, String key, float fallback) {
        captureParam("number", key, fallback);
        Dynamic<?> d = params.get(key);
        return d == null ? fallback : d.asFloat(fallback);
    }

    public static int getInt(Map<String, Dynamic<?>> params, String key, int fallback) {
        captureParam("number", key, fallback);
        Dynamic<?> d = params.get(key);
        return d == null ? fallback : d.asInt(fallback);
    }

    public static long getLong(Map<String, Dynamic<?>> params, String key, long fallback) {
        captureParam("number", key, fallback);
        Dynamic<?> d = params.get(key);
        return d == null ? fallback : d.asLong(fallback);
    }

    public static boolean getBoolean(Map<String, Dynamic<?>> params, String key, boolean fallback) {
        captureParam("boolean", key, fallback);
        Dynamic<?> d = params.get(key);
        return d == null ? fallback : d.asBoolean(fallback);
    }

    public static String getString(Map<String, Dynamic<?>> params, String key, String fallback) {
        captureParam("string", key, fallback);
        Dynamic<?> d = params.get(key);
        return d == null ? fallback : d.asString(fallback);
    }

    /**
     * Captures calls to {@link #getDouble(Map, String, double)} etc to infer parameter keys and defaults.
     * <p>
     * Intended for dev tooling (e.g. the visual editor).
     */
    public static Map<String, CapturedParam> captureParams(Runnable action) {
        if (action == null) {
            return Map.of();
        }
        ParamCapture capture = new ParamCapture();
        PARAM_CAPTURE.set(capture);
        try {
            action.run();
        } finally {
            PARAM_CAPTURE.remove();
        }
        return capture.snapshot();
    }

    private static void captureParam(String kind, String key, Object fallback) {
        if (key == null || key.isEmpty()) {
            return;
        }
        ParamCapture capture = PARAM_CAPTURE.get();
        if (capture == null) {
            return;
        }
        capture.put(kind, key, fallback);
    }

    public record CapturedParam(String kind, Object fallback) {
    }

    private static final class ParamCapture {
        private final Map<String, CapturedParam> params = new HashMap<>();

        void put(String kind, String key, Object fallback) {
            params.putIfAbsent(key, new CapturedParam(kind, fallback));
        }

        Map<String, CapturedParam> snapshot() {
            return params.isEmpty() ? Map.of() : Map.copyOf(params);
        }
    }
}
