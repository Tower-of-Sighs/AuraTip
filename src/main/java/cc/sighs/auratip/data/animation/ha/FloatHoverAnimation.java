package cc.sighs.auratip.data.animation.ha;

import com.mojang.serialization.Dynamic;
import net.minecraft.util.Mth;

import java.util.Map;

import static cc.sighs.auratip.util.SerializationUtil.getDouble;

public class FloatHoverAnimation implements HoverAnimation {
    public static final HoverAnimation INSTANCE = new FloatHoverAnimation(3.0, 0.6f);

    private static final double TWO_PI = Math.PI * 2.0;

    private final double amplitude;
    private final float rampDuration;

    public FloatHoverAnimation(double amplitude, float rampDuration) {
        this.amplitude = amplitude;
        this.rampDuration = rampDuration;
    }

    public static HoverAnimation create(Map<String, Dynamic<?>> params) {
        double amplitude = getDouble(params, "amplitude", 3.0);
        double ramp = getDouble(params, "ramp_duration", 0.6);
        return new FloatHoverAnimation(amplitude, (float) ramp);
    }

    @Override
    public int offsetX(long nowMs, long startMs, int panelWidth, int panelHeight, float speed) {
        return 0;
    }

    @Override
    public int offsetY(long nowMs, long startMs, int panelWidth, int panelHeight, float speed) {
        long elapsed = Math.max(0L, nowMs - startMs);
        float seconds = elapsed / 1000.0f;
        float effectiveSpeed = speed <= 0.0f ? 1.0f : speed;
        double angle = seconds * TWO_PI * effectiveSpeed;
        float ramp = seconds >= rampDuration ? 1.0f : Mth.clamp(seconds / rampDuration, 0.0f, 1.0f);
        double value = Math.sin(angle) * amplitude * ramp;
        return (int) Math.round(value);
    }
}
