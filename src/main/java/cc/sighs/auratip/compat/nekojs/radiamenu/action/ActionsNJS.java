package cc.sighs.auratip.compat.nekojs.radiamenu.action;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.api.action.ActionHandlers;
import cc.sighs.auratip.data.action.Action;
import cc.sighs.auratip.util.SerializationUtil.CapturedParam;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.tkisor.nekojs.NekoJS;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class ActionsNJS {
    private static final Identifier RUN_COMMAND = AuraTip.id("run_command");
    private static final Identifier SIMULATE_KEY = AuraTip.id("simulate_key");

    public static void register(String type, ActionScriptRegistry.ScriptHandler handler) {
        ActionScriptRegistry.register(type, handler);
    }

    public static void register(String type, Map<?, ?> paramDefaults, ActionScriptRegistry.ScriptHandler handler) {
        ActionScriptRegistry.register(type, handler);
        ActionHandlers.declareParamsInternal(normalizeType(type), schemaFrom(paramDefaults));
    }

    public static Action of(String type) {
        if ("run_command".equals(type) || RUN_COMMAND.toString().equals(type)) {
            return new Action.RunCommand("");
        }
        if ("simulate_key".equals(type) || SIMULATE_KEY.toString().equals(type)) {
            return new Action.SimulateKey(0);
        }
        return new Action.ScriptAction(normalizeType(type), Map.of());
    }

    public static Action of(String type, Map<?, ?> params) {
        if ("run_command".equals(type) || RUN_COMMAND.toString().equals(type)) {
            Object cmd = params == null ? null : params.get("command");
            return new Action.RunCommand(cmd == null ? "" : String.valueOf(cmd));
        }
        if ("simulate_key".equals(type) || SIMULATE_KEY.toString().equals(type)) {
            Object code = params == null ? null : params.get("key_code");
            int keyCode;
            if (code instanceof Number n) {
                keyCode = n.intValue();
            } else {
                try {
                    keyCode = code == null ? 0 : Integer.parseInt(String.valueOf(code));
                } catch (NumberFormatException ignored) {
                    keyCode = 0;
                }
            }
            return new Action.SimulateKey(keyCode);
        }

        if (params == null || params.isEmpty()) {
            return new Action.ScriptAction(normalizeType(type), Map.of());
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
        return new Action.ScriptAction(normalizeType(type), result);
    }

    private static Dynamic<?> wrap(Object value) {
        JsonElement element = switch (value) {
            case null -> JsonNull.INSTANCE;
            case Number number -> new JsonPrimitive(number);
            case Boolean bool -> new JsonPrimitive(bool);
            default -> new JsonPrimitive(String.valueOf(value));
        };
        return new Dynamic<>(JsonOps.INSTANCE, element);
    }

    private static Map<String, CapturedParam> schemaFrom(Map<?, ?> paramDefaults) {
        if (paramDefaults == null || paramDefaults.isEmpty()) {
            return Map.of();
        }
        Map<String, CapturedParam> schema = new HashMap<>();
        for (Map.Entry<?, ?> entry : paramDefaults.entrySet()) {
            Object k = entry.getKey();
            if (k == null) continue;
            String key = String.valueOf(k);
            if (key.isEmpty()) continue;
            Object v = entry.getValue();
            if (v instanceof Number n) {
                schema.put(key, new CapturedParam("number", n));
            } else if (v instanceof Boolean b) {
                schema.put(key, new CapturedParam("boolean", b));
            } else if (v != null) {
                schema.put(key, new CapturedParam("string", String.valueOf(v)));
            } else {
                schema.put(key, new CapturedParam("string", ""));
            }
        }
        return schema.isEmpty() ? Map.of() : Map.copyOf(schema);
    }

    private static Identifier normalizeType(String type) {
        if (type == null || type.isEmpty()) {
            return Identifier.fromNamespaceAndPath(NekoJS.MODID, "action");
        }
        if (type.indexOf(':') < 0) {
            return Identifier.fromNamespaceAndPath(NekoJS.MODID, type);
        }
        return Identifier.parse(type);
    }
}
