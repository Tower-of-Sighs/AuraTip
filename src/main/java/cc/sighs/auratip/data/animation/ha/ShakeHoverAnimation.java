package cc.sighs.auratip.data.animation.ha;

import com.mojang.serialization.Dynamic;
import net.minecraft.util.Mth;

import java.util.Map;

import static cc.sighs.auratip.util.SerializationUtil.getDouble;

public class ShakeHoverAnimation implements HoverAnimation {

    public static final HoverAnimation INSTANCE =
            new ShakeHoverAnimation(2.0, 2.0, 2.0, 3.0, Math.PI / 2, 0.4f);

    private static final double TWO_PI = Math.PI * 2.0;

    private final double amplitudeX;
    private final double amplitudeY;
    private final double freqX;
    private final double freqY;
    private final double phase;
    private final float rampDuration;

    public ShakeHoverAnimation(
            double amplitudeX,
            double amplitudeY,
            double freqX,
            double freqY,
            double phase,
            float rampDuration
    ) {
        this.amplitudeX = amplitudeX;
        this.amplitudeY = amplitudeY;
        this.freqX = freqX;
        this.freqY = freqY;
        this.phase = phase;
        this.rampDuration = rampDuration;
    }


    public static HoverAnimation create(Map<String, Dynamic<?>> params) {
        double ampX = getDouble(params, "amplitude_x", 2.0);
        double ampY = getDouble(params, "amplitude_y", 2.0);
        double freqX = getDouble(params, "frequency_x", 2.0);
        double freqY = getDouble(params, "frequency_y", 3.0);
        double phase = getDouble(params, "phase", Math.PI / 2);
        double ramp = getDouble(params, "ramp_duration", 0.4);

        return new ShakeHoverAnimation(
                ampX,
                ampY,
                freqX,
                freqY,
                phase,
                (float) ramp
        );
    }

    private float ramp(float seconds) {
        if (seconds >= rampDuration) return 1.0f;
        return Mth.clamp(seconds / rampDuration, 0.0f, 1.0f);
    }

    @Override
    public int offsetX(long nowMs, long startMs, int panelWidth, int panelHeight, float speed) {
        float seconds = Math.max(0L, nowMs - startMs) / 1000.0f;
        float effectiveSpeed = speed <= 0.0f ? 1.0f : speed;
        float r = ramp(seconds);

        double t = seconds * TWO_PI * effectiveSpeed;
        double x = Math.sin(t * freqX + phase) * amplitudeX * r;

        return (int) Math.round(x);
    }

    @Override
    public int offsetY(long nowMs, long startMs, int panelWidth, int panelHeight, float speed) {
        float seconds = Math.max(0L, nowMs - startMs) / 1000.0f;
        float effectiveSpeed = speed <= 0.0f ? 1.0f : speed;
        float r = ramp(seconds);

        double t = seconds * TWO_PI * effectiveSpeed;
        double y = Math.sin(t * freqY) * amplitudeY * r;

        return (int) Math.round(y);
    }
}
