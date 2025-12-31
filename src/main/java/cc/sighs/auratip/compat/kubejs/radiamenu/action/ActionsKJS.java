package cc.sighs.auratip.compat.kubejs.radiamenu.action;

import cc.sighs.auratip.data.action.Action;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;

import java.util.HashMap;
import java.util.Map;

public class ActionsKJS {
    public static void register(String type, ActionScriptRegistry.ScriptHandler handler) {
        ActionScriptRegistry.register(type, handler);
    }

    public static Action of(String type) {
        return new Action.ScriptAction(type, Map.of());
    }

    public static Action of(String type, Map<?, ?> params) {
        if (params == null || params.isEmpty()) {
            return new Action.ScriptAction(type, Map.of());
        }
        Map<String, Dynamic<?>> result = new HashMap<>();
        for (Map.Entry<?, ?> entry : params.entrySet()) {
            Object keyObj = entry.getKey();
            if (keyObj == null) {
                continue;
            }
            String key = String.valueOf(keyObj);
            Object valueObj = entry.getValue();
            if (valueObj == null) {
                continue;
            }
            result.put(key, wrap(valueObj));
        }
        return new Action.ScriptAction(type, result);
    }

    private static Dynamic<?> wrap(Object value) {
        JsonElement element;
        if (value == null) {
            element = JsonNull.INSTANCE;
        } else if (value instanceof Number number) {
            element = new JsonPrimitive(number);
        } else if (value instanceof Boolean bool) {
            element = new JsonPrimitive(bool);
        } else {
            element = new JsonPrimitive(String.valueOf(value));
        }
        return new Dynamic<>(JsonOps.INSTANCE, element);
    }
}