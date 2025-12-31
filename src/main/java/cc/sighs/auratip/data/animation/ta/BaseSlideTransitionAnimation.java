package cc.sighs.auratip.data.animation.ta;

import cc.sighs.auratip.util.AnimationUtil;
import com.mojang.serialization.Dynamic;

import java.util.Map;
import java.util.function.Function;

import static cc.sighs.auratip.util.SerializationUtil.getFloat;

public abstract class BaseSlideTransitionAnimation implements TransitionAnimation {

    protected final float extraPadding;

    protected BaseSlideTransitionAnimation(float extraPadding) {
        this.extraPadding = extraPadding;
    }
    public static TransitionAnimation create(
            Map<String, Dynamic<?>> params,
            Function<Float, TransitionAnimation> constructor
    ) {
        float padding = getFloat(params, "extra_padding", 24.0f);
        return constructor.apply(padding);
    }

    @Override
    public abstract int offsetX(float eased, int panelWidth, int panelHeight);

    @Override
    public abstract int offsetY(float eased, int panelWidth, int panelHeight);

    @Override
    public float easedProgress(long nowMs, long startMs, boolean closing, int openMs, int closeMs) {
        return AnimationUtil.smoothProgress(nowMs, startMs, closing, openMs, closeMs);
    }
}