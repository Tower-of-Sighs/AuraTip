package cc.sighs.auratip.data.action;

import com.mojang.serialization.Codec;

import java.util.HashMap;
import java.util.Map;

public final class ActionRegistry {

    private static final Map<String, Codec<? extends Action>> TYPES = new HashMap<>();
    private static final Map<Class<?>, String> IDS = new HashMap<>();

    private ActionRegistry() {
    }

    public static <T extends Action> void register(String id, Codec<T> codec, Class<T> type) {
        if (TYPES.containsKey(id)) {
            throw new IllegalStateException("Duplicate action type: " + id);
        }
        TYPES.put(id, codec);
        IDS.put(type, id);
    }

    public static Codec<Action> codec() {
        return Codec.STRING.dispatch(
                "type",
                action -> {
                    String id = IDS.get(action.getClass());
                    if (id != null) {
                        return id;
                    }
                    for (Map.Entry<Class<?>, String> entry : IDS.entrySet()) {
                        if (entry.getKey().isInstance(action)) {
                            return entry.getValue();
                        }
                    }
                    throw new IllegalStateException("Unregistered Action: " + action);
                },
                type -> {
                    Codec<? extends Action> codec = TYPES.get(type);
                    if (codec == null) {
                        throw new IllegalArgumentException("Unknown action type: " + type);
                    }
                    return codec;
                }
        );
    }

    public static void register() {
        register("open_gui", Action.OpenGui.CODEC, Action.OpenGui.class);
        register("run_command", Action.RunCommand.CODEC, Action.RunCommand.class);
        register("simulate_key", Action.SimulateKey.CODEC, Action.SimulateKey.class);
    }
}
