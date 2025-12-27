package cc.sighs.auratip.compat.kubejs;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TipVariables {

    private static final Map<String, StringSupplier> SUPPLIERS = new ConcurrentHashMap<>();

    public static void register(String key, String value) {
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
        SUPPLIERS.put(key, supplier);
    }

    public static void clear(String key) {
        SUPPLIERS.remove(key);
    }

    public static Map<String, String> snapshot() {
        if (SUPPLIERS.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, StringSupplier> entry : SUPPLIERS.entrySet()) {
            var value = entry.getValue().get();
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
}
