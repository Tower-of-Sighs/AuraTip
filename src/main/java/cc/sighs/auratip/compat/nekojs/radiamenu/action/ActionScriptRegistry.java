package cc.sighs.auratip.compat.nekojs.radiamenu.action;

import cc.sighs.auratip.api.action.ActionHandlers;
import com.mojang.serialization.Dynamic;
import com.tkisor.nekojs.NekoJS;
import net.minecraft.resources.Identifier;

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
