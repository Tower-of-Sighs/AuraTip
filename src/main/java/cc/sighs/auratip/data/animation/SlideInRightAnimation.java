package cc.sighs.auratip.data.animation;

import net.minecraft.util.Mth;

public class SlideInRightAnimation implements Animation {
    @Override
    public float easedProgress(long nowMs, long startMs, boolean closing, int openMs, int closeMs) {
        int duration = closing ? closeMs : openMs;
        if (duration <= 0) {
            return closing ? 0.0f : 1.0f;
        }
        float elapsed = (nowMs - startMs) / (float) duration;
        float t = Mth.clamp(elapsed, 0.0f, 1.0f);
        float progress = closing ? 1.0f - t : t;
        return progress * progress * (3.0f - 2.0f * progress);
    }

    @Override
    public int offsetX(float eased, int panelWidth, int panelHeight) {
        return (int) (-(panelWidth + 24.0f) * (1.0f - eased));
    }

    @Override
    public int offsetY(float eased, int panelWidth, int panelHeight) {
        return 0;
    }
}

