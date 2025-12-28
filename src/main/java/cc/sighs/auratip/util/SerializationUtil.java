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

}
