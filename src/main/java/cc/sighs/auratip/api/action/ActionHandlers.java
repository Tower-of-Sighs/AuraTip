package cc.sighs.auratip.api.action;

import cc.sighs.auratip.util.SerializationUtil.CapturedParam;
import com.mojang.serialization.Dynamic;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for custom radial-menu action handlers.
 * <p>
 * When a {@code RadialMenuData.Slot} uses {@link cc.sighs.auratip.data.action.Action.ScriptAction}, AuraTip will look
 * up a handler here using {@code action.type()} and then invoke it with {@code action.params()}.
 * <p>
 * This is client-side behavior because actions are executed when the user clicks a radial menu slot.
 */
public final class ActionHandlers {

    private static final Map<ResourceLocation, Handler> HANDLERS = new ConcurrentHashMap<>();

    /**
     * Optional parameter schema metadata for tooling (e.g. the visual editor).
     * <p>
     * This does not affect runtime behavior; it is only used to render nicer UIs.
     */
    private static final Map<ResourceLocation, Map<String, CapturedParam>> PARAM_SCHEMA = new ConcurrentHashMap<>();

    private ActionHandlers() {
    }

    /**
     * Registers (or replaces) a handler for the given action type.
     */
    public static void register(ResourceLocation type, Handler handler) {
        if (type == null || handler == null) {
            return;
        }
        if (HANDLERS.containsKey(type)) {
            throw new IllegalStateException("Duplicate action handler type: " + type);
        }
        HANDLERS.put(type, handler);
    }

    /**
     * Removes a handler for the given action type.
     */
    public static void clear(ResourceLocation type) {
        if (type == null) {
            return;
        }
        HANDLERS.remove(type);
        PARAM_SCHEMA.remove(type);
    }

    /**
     * Removes all registered handlers.
     */
    public static void clearAll() {
        HANDLERS.clear();
        PARAM_SCHEMA.clear();
    }

    /**
     * Returns all registered handler types.
     */
    public static Set<ResourceLocation> listTypes() {
        return Set.copyOf(HANDLERS.keySet());
    }

    /**
     * Declares a parameter schema for the given action type (tooling-only).
     */
    public static void declareParamsInternal(ResourceLocation type, Map<String, CapturedParam> params) {
        if (type == null || params == null || params.isEmpty()) {
            return;
        }
        PARAM_SCHEMA.put(type, Map.copyOf(params));
    }

    /**
     * Returns the declared parameter schema for a type, or an empty map if none is declared.
     */
    public static Map<String, CapturedParam> getDeclaredParams(ResourceLocation type) {
        if (type == null) {
            return Map.of();
        }
        Map<String, CapturedParam> schema = PARAM_SCHEMA.get(type);
        return schema == null ? Map.of() : schema;
    }

    /**
     * Executes a handler, if present.
     *
     * @param type   action type id
     * @param params param map (nullable). The map is defensively copied before invoking the handler.
     */
    public static void execute(ResourceLocation type, @Nullable Map<String, Dynamic<?>> params) {
        if (type == null) {
            return;
        }
        Handler handler = HANDLERS.get(type);
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

    /**
     * Handler for custom action params.
     */
    @FunctionalInterface
    public interface Handler {
        void execute(Map<String, Dynamic<?>> params);
    }
}
