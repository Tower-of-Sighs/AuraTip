package cc.sighs.auratip.api.animation;

import cc.sighs.auratip.api.util.Params;
import cc.sighs.auratip.data.animation.AnimationType;
import net.minecraft.resources.Identifier;

/**
 * Registers tip animations.
 * <p>
 * Use {@code id} in {@link cc.sighs.auratip.data.TipData.VisualSettings#animationStyle()} and
 * {@link cc.sighs.auratip.data.TipData.VisualSettings#hoverAnimationStyle()}.
 */
public final class TipAnimations {

    private TipAnimations() {
    }

    public static void register(Identifier id, TransitionFactory factory) {
        AnimationType.registerInternal(id, raw -> factory.create(new Params(raw)));
    }

    public static void registerHover(Identifier id, HoverFactory factory) {
        AnimationType.registerHoverInternal(id, raw -> factory.create(new Params(raw)));
    }

    @FunctionalInterface
    public interface TransitionFactory {
        TransitionAnimation create(Params params);
    }

    @FunctionalInterface
    public interface HoverFactory {
        HoverAnimation create(Params params);
    }
}
