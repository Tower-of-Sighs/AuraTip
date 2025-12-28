package cc.sighs.auratip.data.action;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;

import java.util.HashMap;
import java.util.Map;

import static cc.sighs.auratip.util.SerializationUtil.dynamicOf;

public final class ActionRegistry {

    private ActionRegistry() {
    }

    public static Codec<Action> codec() {
        Codec<Map<String, Dynamic<?>>> mapCodec = Codec.unboundedMap(Codec.STRING, Codec.PASSTHROUGH);
        return mapCodec.xmap(ActionRegistry::decode, ActionRegistry::encode);
    }

    private static Action decode(Map<String, Dynamic<?>> raw) {
        if (raw == null || raw.isEmpty()) {
            return new Action.ScriptAction("", Map.of());
        }

        Dynamic<?> typeDyn = raw.get("type");
        String type = typeDyn == null ? "" : typeDyn.asString("");

        switch (type) {
            case "" -> {
                if (raw.containsKey("command")) {
                    String command = raw.get("command").asString("");
                    return new Action.RunCommand(command);
                }
                if (raw.containsKey("key_code")) {
                    int key = raw.get("key_code").asInt(0);
                    return new Action.SimulateKey(key);
                }
                return new Action.ScriptAction("", Map.of());
            }
            case "run_command" -> {
                String command = raw.getOrDefault("command", dynamicOf("")).asString("");
                return new Action.RunCommand(command);
            }
            case "simulate_key" -> {
                int key = raw.getOrDefault("key_code", dynamicOf(0)).asInt(0);
                return new Action.SimulateKey(key);
            }
        }

        Map<String, Dynamic<?>> params = new HashMap<>(raw);
        params.remove("type");
        return new Action.ScriptAction(type, params);
    }

    private static Map<String, Dynamic<?>> encode(Action action) {
        Map<String, Dynamic<?>> out = new HashMap<>();
        if (action instanceof Action.RunCommand rc) {
            out.put("type", dynamicOf("run_command"));
            out.put("command", dynamicOf(rc.command()));
            return out;
        }
        if (action instanceof Action.SimulateKey sk) {
            out.put("type", dynamicOf("simulate_key"));
            out.put("key_code", dynamicOf(sk.keyCode()));
            return out;
        }
        if (action instanceof Action.ScriptAction sa) {
            out.put("type", dynamicOf(sa.type()));
            if (sa.params() != null && !sa.params().isEmpty()) {
                out.putAll(sa.params());
            }
            return out;
        }
        out.put("type", dynamicOf(""));
        return out;
    }
}
