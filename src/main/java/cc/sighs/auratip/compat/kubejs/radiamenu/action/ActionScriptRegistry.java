package cc.sighs.auratip.compat.kubejs.radiamenu.action;

import com.mojang.serialization.Dynamic;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActionScriptRegistry {
    private static final Map<String, ScriptHandler> HANDLERS = new ConcurrentHashMap<>();

    public static void register(String type, ScriptHandler handler) {
        if (type == null || type.isEmpty() || handler == null) {
            return;
        }
        HANDLERS.put(type, handler);
    }

    public static void clear(String type) {
        if (type == null || type.isEmpty()) {
            return;
        }
        HANDLERS.remove(type);
    }

    public static void clearAll() {
        HANDLERS.clear();
    }

    public static void execute(String type, Map<String, Dynamic<?>> params) {
        if (type == null || type.isEmpty()) {
            return;
        }
        ScriptHandler handler = HANDLERS.get(type);
        if (handler == null) {
            return;
        }
        Map<String, Dynamic<?>> safeParams;
        if (params == null || params.isEmpty()) {
            safeParams = Collections.emptyMap();
        } else {
            safeParams = new HashMap<>(params);
        }
        handler.execute(safeParams);
    }

    @FunctionalInterface
    public interface ScriptHandler {
        void execute(Map<String, Dynamic<?>> params);
    }
}