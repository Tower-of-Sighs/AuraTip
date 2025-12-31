const PI = 3.14159265358979323846
const $SerializationUtil = Java.loadClass('cc.sighs.auratip.util.SerializationUtil')

TipAnimations.register('kjs_bounce_left', params => {
    const overshoot = $SerializationUtil.getDouble(params, 'overshoot', 1.70158);
    const extraDistance = $SerializationUtil.getDouble(params, 'extra_distance', 80);

    return {
        easedProgress: (now, start, closing, openMs, closeMs) => {
            const duration = closing ? closeMs : openMs;
            if (duration <= 0) return closing ? 0.0 : 1.0;
            let t = Math.max(0.0, Math.min(1.0, (now - start) / duration));

            if (closing) {
                return 1.0 - (t * t);
            } else {
                t = t - 1.0;
                return t * t * ((overshoot + 1.0) * t + overshoot) + 1.0;
            }
        },

        offsetX: (eased, w, h) => {
            return Math.floor((1.0 - eased) * -(w + extraDistance));
        },

        offsetY: () => 0
    };
});

TipAnimations.registerHover('kjs_jelly', params => {
    const ampX = $SerializationUtil.getDouble(params, 'amplitude_x', 2.0);
    const ampY = $SerializationUtil.getDouble(params, 'amplitude_y', 4.0);
    const freq = $SerializationUtil.getDouble(params, 'frequency_x', 1.0);

    return {
        offsetX: (now, start, w, h, speed) => {
            const seconds = (now - start) / 1000.0;
            const t = seconds * speed * freq;
            return Math.round(Math.sin(t * PI * 2.0) * ampX);
        },

        offsetY: (now, start, w, h, speed) => {
            const seconds = (now - start) / 1000.0;
            const t = seconds * speed * freq;
            return Math.round(Math.cos(t * PI * 2.0) * ampY * 0.5);
        }
    };
});

TipAnimations.registerHover('kjs_float_hover', params => {
    const amplitude = $SerializationUtil.getDouble(params, 'kjs_amplitude', 3.0);
    const rampDuration = $SerializationUtil.getDouble(params, 'kjs_ramp_duration', 0.6);

    const TWO_PI = PI * 2.0;

    return {
        offsetX: (now, start, w, h, speed) => {
            return 0;
        },

        offsetY: (now, start, w, h, speed) => {
            const elapsed = Math.max(0, now - start);
            const seconds = elapsed / 1000.0;

            const effectiveSpeed = speed <= 0.0 ? 1.0 : speed;
            const angle = seconds * TWO_PI * effectiveSpeed;

            const ramp = seconds >= rampDuration
                ? 1.0
                : Math.max(0.0, Math.min(1.0, seconds / rampDuration));

            const value = Math.sin(angle) * amplitude * ramp;
            return Math.round(value);
        }
    };
});