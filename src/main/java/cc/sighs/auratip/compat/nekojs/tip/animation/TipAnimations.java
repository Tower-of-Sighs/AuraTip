package cc.sighs.auratip.compat.nekojs.tip.animation;

import cc.sighs.auratip.data.animation.AnimationType;
import cc.sighs.auratip.util.SerializationUtil.CapturedParam;
import com.mojang.serialization.Dynamic;
import com.tkisor.nekojs.NekoJS;
import graal.graalvm.polyglot.Value;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public class TipAnimations {

    public static void register(String id, Value factory) {
        Identifier rid = normalizeId(id);
        register0(rid, id, factory);
    }

    public static void register(String id, Map<?, ?> paramDefaults, Value factory) {
        Identifier rid = normalizeId(id);
        register0(rid, id, factory);
        AnimationType.declareParamsInternal(rid, schemaFrom(paramDefaults));
    }

    public static void registerHover(String id, Value factory) {
        Identifier rid = normalizeId(id);
        registerHover0(rid, id, factory);
    }

    public static void registerHover(String id, Map<?, ?> paramDefaults, Value factory) {
        Identifier rid = normalizeId(id);
        registerHover0(rid, id, factory);
        AnimationType.declareHoverParamsInternal(rid, schemaFrom(paramDefaults));
    }

    private static void register0(Identifier rid, String id, Value factory) {
        AnimationType.registerOrReplaceInternal(rid, params -> {
            Value obj = callFactory(factory, params);
            if (obj != null && (obj.hasMembers() || obj.canExecute())) {
                return new JsTransitionAnimation(obj);
            }

            throw new IllegalStateException("NJS transition animation must return an object: " + id);
        });
    }

    private static void registerHover0(Identifier rid, String id, Value factory) {
        AnimationType.registerOrReplaceHoverInternal(rid, params -> {
            Value obj = callFactory(factory, params);
            if (obj != null && (obj.hasMembers() || obj.canExecute())) {
                return new JsHoverAnimation(obj);
            }

            throw new IllegalStateException("NJS hover animation must return an object: " + id);
        });
    }

    private static Value callFactory(Value factory, Map<String, Dynamic<?>> params) {
        if (factory == null) {
            return null;
        }
        if (factory.canExecute()) {
            return factory.execute(params);
        }
        if (factory.hasMembers()) {
            Value create = factory.getMember("create");
            if (create != null && create.canExecute()) {
                return create.execute(params);
            }
            return factory;
        }
        return null;
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

    private static Identifier normalizeId(String id) {
        if (id == null || id.isEmpty()) {
            return Identifier.fromNamespaceAndPath(NekoJS.MODID, "animation");
        }
        if (id.indexOf(':') < 0) {
            return Identifier.fromNamespaceAndPath(NekoJS.MODID, id);
        }
        return Identifier.parse(id);
    }
}
