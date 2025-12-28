TipAnimations.register('kjs_bounce_left', params => {

    const overshoot = getDouble(params, 'overshoot', 1.70158);
    const extraDistance = getDouble(params, 'extra_distance', 50);

    return {
        easedProgress: (now, start, closing, openMs, closeMs) => {
            const duration = closing ? closeMs : openMs;
            if (duration <= 0) return closing ? 0.0 : 1.0;

            let t = (now - start) / duration;
            if (t <= 0.0) return closing ? 0.0 : 0.0;
            if (t >= 1.0) return closing ? 0.0 : 1.0;

            if (closing) {
                const u = 1.0 - t;
                return u * u;
            }

            t = t - 1.0;
            return t * t * ((overshoot + 1.0) * t + overshoot) + 1.0;
        },

        offsetX: (eased, w, h) => {
            const distance = w + extraDistance;
            return Math.floor(distance * (1.0 - eased));
        },

        offsetY: () => 0
    };
});

TipAnimations.registerHover('kjs_jelly', params => {

    const ampX = getDouble(params, 'amplitude_x', 1.5);
    const ampY = getDouble(params, 'amplitude_y', 3.0);
    const freqX = getDouble(params, 'frequency_x', 1.2);
    const freqY = getDouble(params, 'frequency_y', 0.8);
    const rampDuration = getDouble(params, 'ramp_duration', 1.0);

    const TWO_PI = Math.PI * 2.0;

    return {
        offsetX: (now, start, w, h, speed) => {
            const seconds = Math.max(0, now - start) / 1000.0;
            const effectiveSpeed = speed <= 0.0 ? 1.0 : speed;
            const ramp = Math.min(seconds / rampDuration, 1.0);

            const angle = seconds * TWO_PI * freqX * effectiveSpeed;
            return Math.round(Math.sin(angle) * ampX * ramp);
        },

        offsetY: (now, start, w, h, speed) => {
            const seconds = Math.max(0, now - start) / 1000.0;
            const effectiveSpeed = speed <= 0.0 ? 1.0 : speed;
            const ramp = Math.min(seconds / rampDuration, 1.0);

            const angle = seconds * TWO_PI * freqY * effectiveSpeed;
            return Math.round(Math.sin(angle) * ampY * ramp);
        }
    };
});


function getDouble(params, key, fallback) {
    const v = params?.get?.(key);
    if (!v) return fallback;
    return v.asDouble(fallback);
}