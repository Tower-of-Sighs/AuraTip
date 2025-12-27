#version 150

in vec2 texCoord0;

uniform vec4 ColorModulator;
uniform vec4 uInnerColor;
uniform vec4 uOuterColor;

uniform float uInnerRadius;  // [0, 0.5] UV 空间
uniform float uOuterRadius;  // [0, 0.5]
uniform float uStartAngle;   // 起始角度（度）
uniform float uEndAngle;     // 结束角度（度）
uniform float uSmooth;       // 抗锯齿宽度
uniform float uFill;       // 填充进度 [0,1]

out vec4 fragColor;

const float PI2 = 6.28318530718;

float atan2_wrap(float y, float x) {
    return mod(atan(y, x) + PI2, PI2);
}

void main() {
    vec2 center = vec2(0.5, 0.5);
    vec2 d = texCoord0 - center;
    float dist = length(d);

    float maxR = mix(uInnerRadius, uOuterRadius, clamp(uFill, 0.0, 1.0));

    // 只到 maxR 为止
    float inRadius = smoothstep(uInnerRadius, uInnerRadius + uSmooth, dist)
    * (1.0 - smoothstep(maxR - uSmooth, maxR, dist));

    if (inRadius <= 0.0) {
        discard;
    }

    // 角度范围：与 Java RadialMenuOverlay 的角度计算保持一致，不再额外旋转 90°
    float angle = atan2_wrap(d.x, -d.y);
    float startRad = radians(uStartAngle);
    float endRad   = radians(uEndAngle);

    float total = endRad - startRad;
    if (total <= 0.0) {
        total += PI2;
    }

    float rel = mod(angle - startRad + PI2, PI2);

    float inAngle;
    if (total >= PI2 - 0.0001) {
        inAngle = 1.0;
    } else {
        inAngle = smoothstep(0.0, uSmooth, rel)
        * (1.0 - smoothstep(total - uSmooth, total, rel));
    }

    if (inAngle <= 0.0) {
        discard;
    }

    // 径向渐变：从内环颜色到外环颜色
    float f = clamp((dist - uInnerRadius) / max(0.0001, (uOuterRadius - uInnerRadius)), 0.0, 1.0);
    vec4 baseColor = mix(uInnerColor, uOuterColor, f) * ColorModulator;

    float mask = inRadius * inAngle;
    baseColor.a *= mask;

    fragColor = baseColor;
}