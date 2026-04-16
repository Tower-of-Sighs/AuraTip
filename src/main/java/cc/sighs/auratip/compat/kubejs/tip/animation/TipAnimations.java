package cc.sighs.auratip.compat.kubejs.tip.animation;

import cc.sighs.auratip.data.animation.AnimationType;
import cc.sighs.auratip.api.animation.HoverAnimation;
import cc.sighs.auratip.api.animation.TransitionAnimation;
import com.mojang.serialization.Dynamic;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.Scriptable;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.function.Function;

public class TipAnimations {

    @Info("Register a custom Tip transition animation type. The factory must return TransitionAnimation or a JS object (auto-wrapped).")
    public static void register(String id, Function<Map<String, Dynamic<?>>, Object> factory) {
        ResourceLocation rid = normalizeId(id);
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

    @Info("Register a custom Tip hover animation type. The factory must return HoverAnimation or a JS object (auto-wrapped).")
    public static void registerHover(String id, Function<Map<String, Dynamic<?>>, Object> factory) {
        ResourceLocation rid = normalizeId(id);
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
