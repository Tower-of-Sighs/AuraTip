package cc.sighs.auratip.data.animation.ta;

import com.mojang.serialization.Dynamic;
import net.minecraft.util.Mth;

import java.util.Map;

public class FadeAndSlideTransitionAnimation implements TransitionAnimation {
    private final float offset;

    public FadeAndSlideTransitionAnimation(float offset) {
        this.offset = offset;
    }

    public static TransitionAnimation create(Map<String, Dynamic<?>> params) {
        float offset = (float) getDouble(params, "offset", 18.0);
        return new FadeAndSlideTransitionAnimation(offset);
    }

    private static double getDouble(Map<String, Dynamic<?>> params, String key, double fallback) {
        Dynamic<?> dynamic = params.get(key);
        return dynamic == null ? fallback : dynamic.asDouble(fallback);
    }

    @Override
    public float easedProgress(long nowMs, long startMs, boolean closing, int openMs, int closeMs) {
        int duration = closing ? closeMs : openMs;
        if (duration <= 0) return closing ? 0.0f : 1.0f;
        float elapsed = (nowMs - startMs) / (float) duration;
        float t = Mth.clamp(elapsed, 0.0f, 1.0f);
        float progress = closing ? 1.0f - t : t;
        return progress * progress * (3.0f - 2.0f * progress);
    }

    @Override
    public int offsetX(float eased, int panelWidth, int panelHeight) {
        return 0;
    }

    @Override
    public int offsetY(float eased, int panelWidth, int panelHeight) {
        return (int) (offset * (1.0f - eased));
    }
}