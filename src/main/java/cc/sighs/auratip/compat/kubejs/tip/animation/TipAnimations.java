package cc.sighs.auratip.compat.kubejs.tip.animation;

import cc.sighs.auratip.data.animation.AnimationType;
import cc.sighs.auratip.data.animation.ha.HoverAnimation;
import cc.sighs.auratip.data.animation.ta.TransitionAnimation;
import com.mojang.serialization.Dynamic;
import dev.latvian.mods.rhino.Scriptable;

import java.util.Map;
import java.util.function.Function;

public class TipAnimations {

    public static void register(String id, Function<Map<String, Dynamic<?>>, Object> factory) {
        AnimationType.registerInternal(id, params -> {
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

    public static void registerHover(String id, Function<Map<String, Dynamic<?>>, Object> factory) {
        AnimationType.registerHoverInternal(id, params -> {
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
}