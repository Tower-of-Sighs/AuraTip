package cc.sighs.auratip.data.action;

import cc.sighs.auratip.AuraTip;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.DataResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static cc.sighs.auratip.util.SerializationUtil.dynamicOf;

public final class ActionRegistry {

    private ActionRegistry() {
    }

    private static final ResourceLocation RUN_COMMAND = new ResourceLocation(AuraTip.MODID, "run_command");
    private static final ResourceLocation SIMULATE_KEY = new ResourceLocation(AuraTip.MODID, "simulate_key");
    private static final ResourceLocation UNKNOWN = new ResourceLocation(AuraTip.MODID, "unknown");

    private static final Map<ResourceLocation, Codec<? extends Action>> CUSTOM_CODECS = new ConcurrentHashMap<>();
    private static final Map<Class<?>, ResourceLocation> CUSTOM_TYPE_BY_CLASS = new ConcurrentHashMap<>();

    public static Codec<Action> codec() {
        Codec<Map<String, Dynamic<?>>> mapCodec = Codec.unboundedMap(Codec.STRING, Codec.PASSTHROUGH);
        return mapCodec.xmap(ActionRegistry::decode, ActionRegistry::encode);
    }

    public static synchronized <T extends Action> void registerCustomCodec(ResourceLocation type, Class<T> actionClass, Codec<T> codec) {
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(actionClass, "actionClass");
        Objects.requireNonNull(codec, "codec");

        if (RUN_COMMAND.equals(type) || SIMULATE_KEY.equals(type) || UNKNOWN.equals(type)) {
            throw new IllegalStateException("Action type '" + type + "' is reserved for built-ins.");
        }

        if (CUSTOM_CODECS.containsKey(type)) {
            throw new IllegalStateException("Duplicate action codec type: " + type);
        }
        if (CUSTOM_TYPE_BY_CLASS.containsKey(actionClass)) {
            throw new IllegalStateException("Action class already registered: " + actionClass.getName());
        }

        CUSTOM_CODECS.put(type, codec);
        CUSTOM_TYPE_BY_CLASS.put(actionClass, type);
    }

    public static synchronized void clearCustomCodec(ResourceLocation type) {
        if (type == null) {
            return;
        }
        Codec<? extends Action> removed = CUSTOM_CODECS.remove(type);
        if (removed == null) {
            return;
        }
        CUSTOM_TYPE_BY_CLASS.entrySet().removeIf(e -> type.equals(e.getValue()));
    }

    private static Action decode(Map<String, Dynamic<?>> raw) {
        if (raw == null || raw.isEmpty()) {
            return new Action.ScriptAction(UNKNOWN, Map.of());
        }

        Dynamic<?> typeDyn = raw.get("type");
        String type = typeDyn == null ? "" : typeDyn.asString("");
        ResourceLocation id = ResourceLocation.tryParse(type);
        if (id == null) {
            throw new IllegalStateException("Invalid action type id: '" + type + "'. Action.type must be a ResourceLocation string like 'modid:path'.");
        }

        if (RUN_COMMAND.equals(id)) {
            String command = raw.getOrDefault("command", dynamicOf("")).asString("");
            return new Action.RunCommand(command);
        }

        if (SIMULATE_KEY.equals(id)) {
            int key = raw.getOrDefault("key_code", dynamicOf(0)).asInt(0);
            return new Action.SimulateKey(key);
        }

        Codec<? extends Action> customCodec = CUSTOM_CODECS.get(id);
        if (customCodec != null) {
            JsonObject json = toJsonObject(raw, false);
            DataResult<? extends Action> parsed = customCodec.parse(JsonOps.INSTANCE, json);
            return parsed.result().orElseThrow(() ->
                    new IllegalStateException("Failed to parse action '" + id + "': " + parsed.error().map(DataResult.PartialResult::message).orElse("unknown error"))
            );
        }

        Map<String, Dynamic<?>> params = new HashMap<>(raw);
        params.remove("type");
        return new Action.ScriptAction(id, params);
    }

    private static Map<String, Dynamic<?>> encode(Action action) {
        Map<String, Dynamic<?>> out = new HashMap<>();
        if (action instanceof Action.RunCommand rc) {
            out.put("type", dynamicOf(RUN_COMMAND.toString()));
            out.put("command", dynamicOf(rc.command()));
            return out;
        }
        if (action instanceof Action.SimulateKey sk) {
            out.put("type", dynamicOf(SIMULATE_KEY.toString()));
            out.put("key_code", dynamicOf(sk.keyCode()));
            return out;
        }
        if (action instanceof Action.ScriptAction sa) {
            out.put("type", dynamicOf(sa.type().toString()));
            if (sa.params() != null && !sa.params().isEmpty()) {
                out.putAll(sa.params());
            }
            return out;
        }

        ResourceLocation type = CUSTOM_TYPE_BY_CLASS.get(action.getClass());
        if (type == null) {
            for (Map.Entry<Class<?>, ResourceLocation> entry : CUSTOM_TYPE_BY_CLASS.entrySet()) {
                if (entry.getKey().isInstance(action)) {
                    type = entry.getValue();
                    break;
                }
            }
        }
        if (type != null) {
            final ResourceLocation finalType = type;
            @SuppressWarnings("unchecked")
            Codec<Action> codec = (Codec<Action>) CUSTOM_CODECS.get(finalType);
            if (codec == null) {
                throw new IllegalStateException("Custom action type '" + finalType + "' has no codec registered.");
            }
            var encodedResult = codec.encodeStart(JsonOps.INSTANCE, action);
            JsonElement encoded = encodedResult.result().orElseThrow(() ->
                    new IllegalStateException("Failed to encode action '" + finalType + "': " + encodedResult.error().map(DataResult.PartialResult::message).orElse("unknown error"))
            );
            if (!(encoded instanceof JsonObject json)) {
                throw new IllegalStateException("Action codec for '" + finalType + "' must encode to a JSON object.");
            }
            out.put("type", dynamicOf(finalType.toString()));
            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                out.put(entry.getKey(), new Dynamic<>(JsonOps.INSTANCE, entry.getValue()));
            }
            return out;
        }

        out.put("type", dynamicOf(UNKNOWN.toString()));
        return out;
    }

    private static JsonObject toJsonObject(Map<String, Dynamic<?>> raw, boolean includeType) {
        JsonObject obj = new JsonObject();
        for (Map.Entry<String, Dynamic<?>> entry : raw.entrySet()) {
            String key = entry.getKey();
            if (key == null || key.isEmpty()) {
                continue;
            }
            if (!includeType && "type".equals(key)) {
                continue;
            }
            Dynamic<?> dyn = entry.getValue();
            if (dyn == null) {
                continue;
            }
            JsonElement value = dyn.convert(JsonOps.INSTANCE).getValue();
            obj.add(key, value);
        }
        return obj;
    }
}
