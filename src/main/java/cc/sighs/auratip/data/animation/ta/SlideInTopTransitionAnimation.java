package cc.sighs.auratip.data.animation.ta;

import com.mojang.serialization.Dynamic;
import net.minecraft.util.Mth;

import java.util.Map;

import static cc.sighs.auratip.util.SerializationUtil.getDouble;

public class SlideInTopTransitionAnimation extends BaseSlideTransitionAnimation {
    public SlideInTopTransitionAnimation(float extraPadding) {
        super(extraPadding);
    }

    public static TransitionAnimation create(Map<String, Dynamic<?>> params) {
        return BaseSlideTransitionAnimation.create(params, SlideInTopTransitionAnimation::new);
    }

    @Override
    public int offsetX(float eased, int panelWidth, int panelHeight) {
        return 0;
    }

    @Override
    public int offsetY(float eased, int panelWidth, int panelHeight) {
        return (int) (-(panelHeight + extraPadding) * (1.0f - eased));
    }
}