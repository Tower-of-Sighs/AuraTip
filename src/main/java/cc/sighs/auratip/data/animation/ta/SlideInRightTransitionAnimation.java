package cc.sighs.auratip.data.animation.ta;

import com.mojang.serialization.Dynamic;
import net.minecraft.util.Mth;

import java.util.Map;

import static cc.sighs.auratip.util.SerializationUtil.getDouble;

public class SlideInRightTransitionAnimation extends BaseSlideTransitionAnimation {
    public SlideInRightTransitionAnimation(float extraPadding) {
        super(extraPadding);
    }

    public static TransitionAnimation create(Map<String, Dynamic<?>> params) {
        return BaseSlideTransitionAnimation.create(params, SlideInRightTransitionAnimation::new);
    }

    @Override
    public int offsetX(float eased, int panelWidth, int panelHeight) {
        return (int) (-(panelWidth + extraPadding) * (1.0f - eased));
    }

    @Override
    public int offsetY(float eased, int panelWidth, int panelHeight) {
        return 0;
    }
}