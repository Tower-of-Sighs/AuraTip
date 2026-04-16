package cc.sighs.auratip.compat.kubejs.radiamenu.action;

import cc.sighs.auratip.api.action.ActionHandlers;
import com.mojang.serialization.Dynamic;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class ActionScriptRegistry {
    public static void register(String type, ScriptHandler handler) {
        ActionHandlers.register(normalizeType(type), handler::execute);
    }

    public static void clear(String type) {
        ActionHandlers.clear(normalizeType(type));
    }

    public static void clearAll() {
        ActionHandlers.clearAll();
    }

    public static void execute(String type, Map<String, Dynamic<?>> params) {
        ActionHandlers.execute(normalizeType(type), params);
    }

    @FunctionalInterface
    public interface ScriptHandler {
        void execute(Map<String, Dynamic<?>> params);
    }

    private static ResourceLocation normalizeType(String type) {
        if (type == null || type.isEmpty()) {
            return new ResourceLocation("kubejs", "action");
        }
        if (type.indexOf(':') < 0) {
            return new ResourceLocation("kubejs", type);
        }
        return new ResourceLocation(type);
    }
}
