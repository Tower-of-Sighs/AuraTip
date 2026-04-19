package cc.sighs.auratip.compat.kubejs.radiamenu.action;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.api.action.ActionHandlers;
import cc.sighs.auratip.data.action.Action;
import cc.sighs.auratip.util.SerializationUtil.CapturedParam;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class ActionsKJS {
    private static final ResourceLocation RUN_COMMAND = AuraTip.id("run_command");
    private static final ResourceLocation SIMULATE_KEY = AuraTip.id("simulate_key");

    @Info("Register a script-backed action handler. The type can then be used as a radial menu slot action to invoke the callback.")
    public static void register(String type, ActionScriptRegistry.ScriptHandler handler) {
        ActionScriptRegistry.register(type, handler);
    }

    @Info("Register a script-backed action handler with parameter defaults (tooling-only). paramDefaults is a map of key -> default value.")
    public static void register(String type, Map<?, ?> paramDefaults, ActionScriptRegistry.ScriptHandler handler) {
        ActionScriptRegistry.register(type, handler);
        ActionHandlers.declareParamsInternal(normalizeType(type), schemaFrom(paramDefaults));
    }

    @Info("Create an Action without params. Built-in run_command / simulate_key create the matching Action; other types create a script action.")
    public static Action of(String type) {
        if ("run_command".equals(type) || RUN_COMMAND.toString().equals(type)) {
            return new Action.RunCommand("");
        }
        if ("simulate_key".equals(type) || SIMULATE_KEY.toString().equals(type)) {
            return new Action.SimulateKey(0);
        }
        return new Action.ScriptAction(normalizeType(type), Map.of());
    }

    @Info("Create an Action with params. run_command uses params.command; simulate_key uses params.key_code; other types pass params as dynamic values.")
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

    private static ResourceLocation normalizeType(String type) {
        if (type == null || type.isEmpty()) {
            return ResourceLocation.fromNamespaceAndPath("kubejs", "action");
        }
        if (type.indexOf(':') < 0) {
            return ResourceLocation.fromNamespaceAndPath("kubejs", type);
        }
        return ResourceLocation.parse(type);
    }
}
