package cc.sighs.auratip.compat.kubejs.tip;

import net.minecraft.network.chat.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TipVariables {

    private static final Map<String, ComponentSupplier> SUPPLIERS = new ConcurrentHashMap<>();

    public static void register(String key, String value) {
        if (key == null || key.isEmpty()) {
            return;
        }
        if (value == null) {
            SUPPLIERS.remove(key);
        } else {
            SUPPLIERS.put(key, () -> Component.literal(value));
        }
    }

    public static void registerComponent(String key, Component value) {
        if (key == null || key.isEmpty()) {
            return;
        }
        if (value == null) {
            SUPPLIERS.remove(key);
        } else {
            SUPPLIERS.put(key, () -> value);
        }
    }

    public static void registerDynamic(String key, StringSupplier supplier) {
        if (key == null || key.isEmpty() || supplier == null) {
            return;
        }
        SUPPLIERS.put(key, () -> {
            String value = supplier.get();
            if (value == null) {
                return null;
            }
            return Component.literal(value);
        });
    }

    public static void registerDynamicComponent(String key, ComponentSupplier supplier) {
        if (key == null || key.isEmpty() || supplier == null) {
            return;
        }
        SUPPLIERS.put(key, supplier);
    }

    public static void clear(String key) {
        SUPPLIERS.remove(key);
    }

    public static Map<String, Component> snapshot() {
        if (SUPPLIERS.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Component> result = new HashMap<>();
        for (Map.Entry<String, ComponentSupplier> entry : SUPPLIERS.entrySet()) {
            Component value = entry.getValue().get();
            if (value != null) {
                result.put(entry.getKey(), value);
            }
        }
        return result;
    }

    @FunctionalInterface
    public interface StringSupplier {
        String get();
    }

    @FunctionalInterface
    public interface ComponentSupplier {
        Component get();
    }
}
