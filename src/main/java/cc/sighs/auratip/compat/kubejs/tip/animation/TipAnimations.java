package cc.sighs.auratip.compat.kubejs.tip.animation;

import cc.sighs.auratip.data.animation.AnimationType;
import cc.sighs.auratip.api.animation.HoverAnimation;
import cc.sighs.auratip.api.animation.TransitionAnimation;
import cc.sighs.auratip.util.SerializationUtil.CapturedParam;
import com.mojang.serialization.Dynamic;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.Scriptable;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class TipAnimations {

    @Info("Register a custom Tip transition animation type. The factory must return TransitionAnimation or a JS object (auto-wrapped).")
    public static void register(String id, Function<Map<String, Dynamic<?>>, Object> factory) {
        ResourceLocation rid = normalizeId(id);
        register0(rid, id, factory);
    }

    @Info("Register a custom Tip transition animation type with parameter defaults (tooling-only). paramDefaults is a map of key -> default value.")
    public static void register(String id, Map<?, ?> paramDefaults, Function<Map<String, Dynamic<?>>, Object> factory) {
        ResourceLocation rid = normalizeId(id);
        register0(rid, id, factory);
        AnimationType.declareParamsInternal(rid, schemaFrom(paramDefaults));
    }

    @Info("Register a custom Tip hover animation type. The factory must return HoverAnimation or a JS object (auto-wrapped).")
    public static void registerHover(String id, Function<Map<String, Dynamic<?>>, Object> factory) {
        ResourceLocation rid = normalizeId(id);
        registerHover0(rid, id, factory);
    }

    @Info("Register a custom Tip hover animation type with parameter defaults (tooling-only). paramDefaults is a map of key -> default value.")
    public static void registerHover(String id, Map<?, ?> paramDefaults, Function<Map<String, Dynamic<?>>, Object> factory) {
        ResourceLocation rid = normalizeId(id);
        registerHover0(rid, id, factory);
        AnimationType.declareHoverParamsInternal(rid, schemaFrom(paramDefaults));
    }

    private static void register0(ResourceLocation rid, String id, Function<Map<String, Dynamic<?>>, Object> factory) {
        AnimationType.registerInternal(rid, params -> {
            Object obj = factory.apply(params);
            if (obj instanceof TransitionAnimation ta) {
                return ta;
            }
            if (obj instanceof Scriptable s) {
                return new JsTransitionAnimation(s);
            }
            throw new IllegalStateException("KJS transition animation must return an object: " + id);
        });
    }

    private static void registerHover0(ResourceLocation rid, String id, Function<Map<String, Dynamic<?>>, Object> factory) {
        AnimationType.registerHoverInternal(rid, params -> {
            Object obj = factory.apply(params);
            if (obj instanceof HoverAnimation ha) {
                return ha;
            }
            if (obj instanceof Scriptable s) {
                return new JsHoverAnimation(s);
            }
            throw new IllegalStateException("KJS hover animation must return an object: " + id);
        });
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

    private static ResourceLocation normalizeId(String id) {
        if (id == null || id.isEmpty()) {
            return ResourceLocation.fromNamespaceAndPath("kubejs", "animation");
        }
        if (id.indexOf(':') < 0) {
            return ResourceLocation.fromNamespaceAndPath("kubejs", id);
        }
        return ResourceLocation.parse(id);
    }
}
