package cc.sighs.auratip.data.animation.ta;

import cc.sighs.auratip.util.AnimationUtil;
import com.mojang.serialization.Dynamic;

import java.util.Map;

public class SlideTransitionAnimation extends BaseSlideTransitionAnimation {
    public SlideTransitionAnimation(float extraPadding) {
        super(extraPadding);
    }

    public static TransitionAnimation create(Map<String, Dynamic<?>> params) {
        return BaseSlideTransitionAnimation.create(params, SlideTransitionAnimation::new);
    }

    @Override
    public int offsetX(float eased, int panelWidth, int panelHeight) {
        return 0;
    }

    @Override
    public int offsetY(float eased, int panelWidth, int panelHeight) {
        return (int) ((panelHeight + extraPadding) * (1.0f - eased));
    }

    @Override
    public float easedProgress(long nowMs, long startMs, boolean closing, int openMs, int closeMs) {
        return AnimationUtil.linearProgress(nowMs, startMs, closing, openMs, closeMs);
    }
}