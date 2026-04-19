package cc.sighs.auratip.api.action;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.api.util.Params;
import cc.sighs.auratip.data.action.Action;
import cc.sighs.auratip.data.action.ActionRegistry;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static cc.sighs.auratip.util.SerializationUtil.convertMapToDynamic;

/**
 * Action helpers.
 * <p>
 * - Built-ins: {@link #runCommand(String)} / {@link #simulateKey(int)}
 * - Script actions: {@link #script(ResourceLocation, Map)} + {@link #register(ResourceLocation, ParamsHandler)}
 * - Datapack codecs: {@link #registerCodec(ResourceLocation, Class, Codec)}
 * - Java actions: implement {@link Action} and {@link #register(Class, Consumer)}
 */
public final class Actions {

    private Actions() {
    }

    private static final Map<Class<?>, Consumer<?>> TYPED = new ConcurrentHashMap<>();

    /**
     * Creates a built-in "run command" action.
     * <p>
     * The command is sent as a client-side chat command (leading '/' is optional).
     */
    public static Action runCommand(String command) {
        return new Action.RunCommand(command == null ? "" : command);
    }

    /**
     * Creates a built-in "simulate key" action.
     * <p>
     * Note: currently only a small subset of keys may have effects (see {@code ActionExecutor}).
     */
    public static Action simulateKey(int keyCode) {
        return new Action.SimulateKey(keyCode);
    }

    /**
     * Creates a custom action with {@code type} and arbitrary params.
     * <p>
     * The action will be executed only if you register a handler for {@code type} via {@link #register(ResourceLocation, ParamsHandler)}
     * (or {@link #registerRaw(ResourceLocation, RawHandler)}).
     */
    public static Action script(ResourceLocation type, @Nullable Map<String, ?> params) {
        if (type == null) {
            return new Action.ScriptAction(AuraTip.id("unknown"), Map.of());
        }
        if (params == null || params.isEmpty()) {
            return new Action.ScriptAction(type, Map.of());
        }
        Map<String, Object> safe = new HashMap<>();
        for (Map.Entry<String, ?> entry : params.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key == null || key.isEmpty() || value == null) {
                continue;
            }
            safe.put(key, value);
        }
        return new Action.ScriptAction(type, convertMapToDynamic(safe));
    }

    /**
     * Creates a custom script action using an already-built {@code Map<String, Dynamic<?>>}.
     */
    public static Action scriptRaw(ResourceLocation type, @Nullable Map<String, Dynamic<?>> params) {
        if (type == null) {
            return new Action.ScriptAction(AuraTip.id("unknown"), Map.of());
        }
        if (params == null || params.isEmpty()) {
            return new Action.ScriptAction(type, Map.of());
        }
        return new Action.ScriptAction(type, Map.copyOf(params));
    }

    /**
     * Registers (or replaces) a custom action handler.
     *
     * @param type    action type id
     * @param handler handler implementation
     */
    public static void register(ResourceLocation type, ParamsHandler handler) {
        if (type == null || handler == null) {
            return;
        }
        ActionHandlers.register(type, raw -> handler.execute(new Params(raw)));
    }

    /**
     * Registers a custom action handler using the raw {@code Map<String, Dynamic<?>>} params.
     */
    public static void registerRaw(ResourceLocation type, RawHandler handler) {
        if (type == null || handler == null) {
            return;
        }
        ActionHandlers.register(type, handler::execute);
    }

    /**
     * Clears a custom action handler by {@code type}.
     */
    public static void clear(ResourceLocation type) {
        if (type == null) {
            return;
        }
        ActionHandlers.clear(type);
    }

    /**
     * Clears all custom action handlers (script actions).
     */
    public static void clearAll() {
        ActionHandlers.clearAll();
    }

    /**
     * Registers a custom action codec so your {@link Action} implementation can be used in datapacks
     * (and anywhere else that relies on {@link Action#CODEC}).
     * <p>
     * The corresponding JSON shape is:
     * <pre>
     * {
     *   "type": "modid:my_action",
     *   ... your codec fields ...
     * }
     * </pre>
     *
     * @param type        action type id (must be unique)
     * @param actionClass action implementation class
     * @param codec       codec for fields excluding {@code type}
     */
    public static <T extends Action> void registerCodec(ResourceLocation type, Class<T> actionClass, Codec<T> codec) {
        ActionRegistry.registerCustomCodec(type, actionClass, codec);
    }

    /**
     * Clears a custom action codec by {@code type}.
     */
    public static void clearCodec(ResourceLocation type) {
        ActionRegistry.clearCustomCodec(type);
    }

    /**
     * Registers an executor for a custom Java {@link Action} implementation.
     * <p>
     * This is for actions that are created directly in Java and do not need to be deserialized from datapacks.
     */
    public static <T extends Action> void register(Class<T> actionClass, Consumer<T> executor) {
        if (actionClass == null || executor == null) {
            return;
        }
        TYPED.put(actionClass, executor);
    }

    /**
     * Removes an executor for a custom Java {@link Action} implementation.
     */
    public static void clear(Class<? extends Action> actionClass) {
        if (actionClass == null) {
            return;
        }
        TYPED.remove(actionClass);
    }

    /**
     * Called by the engine when a slot is clicked.
     */
    public static void executeTyped(Action action) {
        if (action == null) {
            return;
        }
        Consumer<?> exact = TYPED.get(action.getClass());
        if (exact != null) {
            @SuppressWarnings("unchecked")
            Consumer<Action> typed = (Consumer<Action>) exact;
            typed.accept(action);
            return;
        }
        for (Map.Entry<Class<?>, Consumer<?>> entry : TYPED.entrySet()) {
            Class<?> type = entry.getKey();
            if (type.isInstance(action)) {
                @SuppressWarnings("unchecked")
                Consumer<Action> typed = (Consumer<Action>) entry.getValue();
                typed.accept(action);
                return;
            }
        }
    }

    @FunctionalInterface
    public interface ParamsHandler {
        void execute(Params params);
    }

    @FunctionalInterface
    public interface RawHandler {
        void execute(Map<String, Dynamic<?>> params);
    }
}
