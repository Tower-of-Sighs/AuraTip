package cc.sighs.auratip.data.animation;

import cc.sighs.auratip.data.animation.ha.FloatHoverAnimation;
import cc.sighs.auratip.data.animation.ha.HoverAnimation;
import cc.sighs.auratip.data.animation.ha.NoneHoverAnimation;
import cc.sighs.auratip.data.animation.ha.ShakeHoverAnimation;
import cc.sighs.auratip.data.animation.ta.*;
import com.mojang.serialization.Dynamic;

import java.util.*;

public final class AnimationType {
    private static final Map<String, AnimationFactory> ANIMATIONS = new HashMap<>();
    private static final Map<String, HoverAnimationFactory> HOVER_ANIMATIONS = new HashMap<>();
    private static final String DEFAULT_ID = "fade_and_slide";
    private static final String DEFAULT_HOVER_ID = "none";

    static {
        registerInternal(DEFAULT_ID, FadeAndSlideTransitionAnimation::create);
        registerInternal("fade", FadeTransitionAnimation::create);
        registerInternal("slide", SlideTransitionAnimation::create);
        registerInternal("slide_in_left", SlideInLeftTransitionAnimation::create);
        registerInternal("slide_in_right", SlideInRightTransitionAnimation::create);
        registerInternal("slide_in_top", SlideInTopTransitionAnimation::create);
        registerInternal("slide_in_bottom", SlideTransitionAnimation::create);

        registerHoverInternal(DEFAULT_HOVER_ID, params -> NoneHoverAnimation.INSTANCE);
        registerHoverInternal("hover_float", FloatHoverAnimation::create);
        registerHoverInternal("hover_shake", ShakeHoverAnimation::create);
    }

    private AnimationType() {
    }

    public static TransitionAnimation resolve(String id) {
        return resolve(id, Map.of());
    }

    public static TransitionAnimation resolve(String id, Map<String, Dynamic<?>> params) {
        String key = normalize(id, DEFAULT_ID);
        AnimationFactory factory = ANIMATIONS.get(key);
        if (factory == null) {
            factory = ANIMATIONS.get(DEFAULT_ID);
        }
        return factory.create(params == null ? Map.of() : params);
    }

    public static HoverAnimation resolveHover(String id) {
        return resolveHover(id, Map.of());
    }

    public static HoverAnimation resolveHover(String id, Map<String, Dynamic<?>> params) {
        String key = normalize(id, DEFAULT_HOVER_ID);
        HoverAnimationFactory factory = HOVER_ANIMATIONS.get(key);
        if (factory == null) {
            factory = HOVER_ANIMATIONS.get(DEFAULT_HOVER_ID);
        }
        return factory.create(params == null ? Map.of() : params);
    }

    public static void registerInternal(String id, AnimationFactory factory) {
        if (id == null || id.isBlank() || factory == null) {
            return;
        }
        String key = id.trim().toLowerCase(Locale.ROOT);
        ANIMATIONS.put(key, factory);
    }

    public static void registerHoverInternal(String id, HoverAnimationFactory factory) {
        if (id == null || id.isBlank() || factory == null) {
            return;
        }
        String key = id.trim().toLowerCase(Locale.ROOT);
        HOVER_ANIMATIONS.put(key, factory);
    }

    private static String normalize(String id, String defaultId) {
        if (id == null || id.isBlank()) {
            return defaultId;
        }
        return id.trim().toLowerCase(Locale.ROOT);
    }

    public interface AnimationFactory {
        TransitionAnimation create(Map<String, Dynamic<?>> params);
    }

    public interface HoverAnimationFactory {
        HoverAnimation create(Map<String, Dynamic<?>> params);
    }
}
