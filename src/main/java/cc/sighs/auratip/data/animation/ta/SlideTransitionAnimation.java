package cc.sighs.auratip.data.animation.ta;

import com.mojang.serialization.Dynamic;
import net.minecraft.util.Mth;

import java.util.Map;

public class SlideTransitionAnimation implements TransitionAnimation {
    private final float extraPadding;

    public SlideTransitionAnimation(float extraPadding) {
        this.extraPadding = extraPadding;
    }

    public static TransitionAnimation create(Map<String, Dynamic<?>> params) {
        float padding = (float) getDouble(params, "extra_padding", 24.0);
        return new SlideTransitionAnimation(padding);
    }

    private static double getDouble(Map<String, Dynamic<?>> params, String key, double fallback) {
        Dynamic<?> dynamic = params.get(key);
        return dynamic == null ? fallback : dynamic.asDouble(fallback);
    }

    @Override
    public float easedProgress(long nowMs, long startMs, boolean closing, int openMs, int closeMs) {
        int duration = closing ? closeMs : openMs;
        if (duration <= 0) {
            return closing ? 0.0f : 1.0f;
        }
        float elapsed = (nowMs - startMs) / (float) duration;
        float t = Mth.clamp(elapsed, 0.0f, 1.0f);
        float progress = closing ? 1.0f - t : t;
        return Mth.clamp(progress, 0.0f, 1.0f);
    }

    @Override
    public int offsetX(float eased, int panelWidth, int panelHeight) {
        return 0;
    }

    @Override
    public int offsetY(float eased, int panelWidth, int panelHeight) {
        return (int) ((panelHeight + extraPadding) * (1.0f - eased));
    }
}
