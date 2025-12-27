package cc.sighs.auratip.data.animation;

import net.minecraft.util.Mth;

public class SlideAnimation implements Animation {
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
        return (int) ((panelHeight + 24.0f) * (1.0f - eased));
    }
}
