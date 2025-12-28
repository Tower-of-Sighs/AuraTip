package cc.sighs.auratip.compat.kubejs.tip;

import cc.sighs.auratip.data.animation.AnimationType;
import cc.sighs.auratip.data.animation.ha.HoverAnimation;
import cc.sighs.auratip.data.animation.ta.TransitionAnimation;
import com.mojang.serialization.Dynamic;

import java.util.Map;
import java.util.function.Function;

public class TipAnimations {

    public static void register(String id, Function<Map<String, Dynamic<?>>, TransitionAnimation> factory) {
        AnimationType.registerInternal(id, factory::apply);
    }

    public static void registerHover(String id, Function<Map<String, Dynamic<?>>, HoverAnimation> factory) {
        AnimationType.registerHoverInternal(id, factory::apply);
    }
}
