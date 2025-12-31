package cc.sighs.auratip.data.animation.ta;

import com.mojang.serialization.Dynamic;
import net.minecraft.util.Mth;

import java.util.Map;

public class FadeTransitionAnimation implements TransitionAnimation {
    public static TransitionAnimation create(Map<String, Dynamic<?>> params) {
        return new FadeTransitionAnimation();
    }

    @Override
    public float easedProgress(long nowMs, long startMs, boolean closing, int openMs, int closeMs) {
        int duration = closing ? closeMs : openMs;
        if (duration <= 0) return closing ? 0.0f : 1.0f;
        float elapsed = (nowMs - startMs) / (float) duration;
        float t = Mth.clamp(elapsed, 0.0f, 1.0f);
        return closing ? 1.0f - t : t;
    }

    @Override
    public int offsetX(float eased, int panelWidth, int panelHeight) {
        return 0;
    }

    @Override
    public int offsetY(float eased, int panelWidth, int panelHeight) {
        return 0;
    }
}