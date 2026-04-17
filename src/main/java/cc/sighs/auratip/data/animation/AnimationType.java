package cc.sighs.auratip.data.animation;

import cc.sighs.auratip.data.animation.ha.FloatHoverAnimation;
import cc.sighs.auratip.data.animation.ha.NoneHoverAnimation;
import cc.sighs.auratip.data.animation.ha.ShakeHoverAnimation;
import cc.sighs.auratip.data.animation.ta.*;
import cc.sighs.auratip.api.animation.HoverAnimation;
import cc.sighs.auratip.api.animation.TransitionAnimation;
import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.util.SerializationUtil.CapturedParam;
import com.mojang.serialization.Dynamic;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public final class AnimationType {
    private static final Map<ResourceLocation, AnimationFactory> ANIMATIONS = new HashMap<>();
    private static final Map<ResourceLocation, HoverAnimationFactory> HOVER_ANIMATIONS = new HashMap<>();

    /**
     * Optional parameter schema metadata for the visual editor / tooling.
     * <p>
     * Built-in animations may rely on editor-side inference (capturing SerializationUtil.get* calls),
     * but script-registered animations can declare their params explicitly.
     */
    private static final Map<ResourceLocation, Map<String, CapturedParam>> ANIMATION_PARAM_SCHEMA = new HashMap<>();
    private static final Map<ResourceLocation, Map<String, CapturedParam>> HOVER_PARAM_SCHEMA = new HashMap<>();

    private static final ResourceLocation DEFAULT_ID = AuraTip.id("fade_and_slide");
    private static final ResourceLocation DEFAULT_HOVER_ID = AuraTip.id("none");

    static {
        registerInternal(DEFAULT_ID, FadeAndSlideTransitionAnimation::create);
        registerInternal(AuraTip.id("fade"), FadeTransitionAnimation::create);
        registerInternal(AuraTip.id("slide"), SlideTransitionAnimation::create);
        registerInternal(AuraTip.id("slide_in_left"), SlideInLeftTransitionAnimation::create);
        registerInternal(AuraTip.id("slide_in_right"), SlideInRightTransitionAnimation::create);
        registerInternal(AuraTip.id("slide_in_top"), SlideInTopTransitionAnimation::create);
        registerInternal(AuraTip.id("slide_in_bottom"), SlideTransitionAnimation::create);

        registerHoverInternal(DEFAULT_HOVER_ID, params -> NoneHoverAnimation.INSTANCE);
        registerHoverInternal(AuraTip.id("hover_float"), FloatHoverAnimation::create);
        registerHoverInternal(AuraTip.id("hover_shake"), ShakeHoverAnimation::create);
    }

    private AnimationType() {
    }

    public static TransitionAnimation resolve(ResourceLocation id) {
        return resolve(id, Map.of());
    }

    public static TransitionAnimation resolve(ResourceLocation id, Map<String, Dynamic<?>> params) {
        ResourceLocation key = (id == null) ? DEFAULT_ID : id;
        AnimationFactory factory = ANIMATIONS.get(key);
        if (factory == null) {
            factory = ANIMATIONS.get(DEFAULT_ID);
        }
        return factory.create(params == null ? Map.of() : params);
    }

    public static HoverAnimation resolveHover(ResourceLocation id) {
        return resolveHover(id, Map.of());
    }

    public static HoverAnimation resolveHover(ResourceLocation id, Map<String, Dynamic<?>> params) {
        ResourceLocation key = (id == null) ? DEFAULT_HOVER_ID : id;
        HoverAnimationFactory factory = HOVER_ANIMATIONS.get(key);
        if (factory == null) {
            factory = HOVER_ANIMATIONS.get(DEFAULT_HOVER_ID);
        }
        return factory.create(params == null ? Map.of() : params);
    }

    public static void registerInternal(ResourceLocation id, AnimationFactory factory) {
        if (id == null || factory == null) {
            return;
        }
        if (ANIMATIONS.containsKey(id)) {
            throw new IllegalStateException("Duplicate transition animation id: " + id);
        }
        ANIMATIONS.put(id, factory);
    }

    /**
     * Declares parameter schema metadata for a transition animation type.
     * <p>
     * This does not affect runtime behavior; it is consumed by tooling (e.g. the visual editor).
     */
    public static void declareParamsInternal(ResourceLocation id, Map<String, CapturedParam> params) {
        if (id == null || params == null || params.isEmpty()) {
            return;
        }
        ANIMATION_PARAM_SCHEMA.put(id, Map.copyOf(params));
    }

    /**
     * Registers or replaces a transition animation factory.
     * <p>
     * Intended for dev tooling (e.g. editor hot-reload).
     */
    public static void registerOrReplaceInternal(ResourceLocation id, AnimationFactory factory) {
        if (id == null || factory == null) {
            return;
        }
        ANIMATIONS.put(id, factory);
    }

    public static void registerHoverInternal(ResourceLocation id, HoverAnimationFactory factory) {
        if (id == null || factory == null) {
            return;
        }
        if (HOVER_ANIMATIONS.containsKey(id)) {
            throw new IllegalStateException("Duplicate hover animation id: " + id);
        }
        HOVER_ANIMATIONS.put(id, factory);
    }

    /**
     * Declares parameter schema metadata for a hover animation type.
     * <p>
     * This does not affect runtime behavior; it is consumed by tooling (e.g. the visual editor).
     */
    public static void declareHoverParamsInternal(ResourceLocation id, Map<String, CapturedParam> params) {
        if (id == null || params == null || params.isEmpty()) {
            return;
        }
        HOVER_PARAM_SCHEMA.put(id, Map.copyOf(params));
    }

    /**
     * Registers or replaces a hover animation factory.
     * <p>
     * Intended for dev tooling (e.g. editor hot-reload).
     */
    public static void registerOrReplaceHoverInternal(ResourceLocation id, HoverAnimationFactory factory) {
        if (id == null || factory == null) {
            return;
        }
        HOVER_ANIMATIONS.put(id, factory);
    }

    public interface AnimationFactory {
        TransitionAnimation create(Map<String, Dynamic<?>> params);
    }

    public interface HoverAnimationFactory {
        HoverAnimation create(Map<String, Dynamic<?>> params);
    }

    /**
     * Returns all registered transition animation ids (including KubeJS-registered ones).
     */
    public static Set<ResourceLocation> listTransitionIds() {
        return Set.copyOf(ANIMATIONS.keySet());
    }

    /**
     * Returns all registered hover animation ids (including KubeJS-registered ones).
     */
    public static Set<ResourceLocation> listHoverIds() {
        return Set.copyOf(HOVER_ANIMATIONS.keySet());
    }

    /**
     * Returns declared parameter schema metadata for a transition animation type, if any.
     */
    public static Map<String, CapturedParam> getDeclaredParams(ResourceLocation id) {
        if (id == null) {
            return Map.of();
        }
        Map<String, CapturedParam> schema = ANIMATION_PARAM_SCHEMA.get(id);
        return schema == null ? Map.of() : schema;
    }

    /**
     * Returns declared parameter schema metadata for a hover animation type, if any.
     */
    public static Map<String, CapturedParam> getDeclaredHoverParams(ResourceLocation id) {
        if (id == null) {
            return Map.of();
        }
        Map<String, CapturedParam> schema = HOVER_PARAM_SCHEMA.get(id);
        return schema == null ? Map.of() : schema;
    }
}
