#version 330

// Copied layout from minecraft:dynamictransforms.glsl
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};

in vec4 vertexColor; // outer color
in vec2 guiPos;
in vec2 ringCenter;
flat in ivec2 innerColorPacked;
flat in ivec2 anglePacked;
flat in uint radiiBits;
flat in vec3 packedParams;

out vec4 fragColor;

const float PI2 = 6.28318530718;
const float SMOOTH_PACK_MAX = 8.0;

float atan2_wrap(float y, float x) {
    return mod(atan(y, x) + PI2, PI2);
}

vec4 unpackArgb(uint argb) {
    float a = float((argb >> 24) & 0xFFu) / 255.0;
    float r = float((argb >> 16) & 0xFFu) / 255.0;
    float g = float((argb >> 8) & 0xFFu) / 255.0;
    float b = float((argb >> 0) & 0xFFu) / 255.0;
    return vec4(r, g, b, a);
}

uint unpackU16(int v) {
    return uint(v) & 0xFFFFu;
}

void main() {
    float innerRadius = float(radiiBits & 0xFFFFu) / 16.0;
    float outerRadius = float((radiiBits >> 16) & 0xFFFFu) / 16.0;
    if (outerRadius <= innerRadius + 0.0001) {
        discard;
    }

    float fill = clamp((packedParams.x + 1.0) * 0.5, 0.0, 1.0);
    float smoothPixels = max(0.0, clamp((packedParams.y + 1.0) * 0.5, 0.0, 1.0) * SMOOTH_PACK_MAX);
    float maxR = mix(innerRadius, outerRadius, fill);

    uint innerBits = unpackU16(innerColorPacked.x) | (unpackU16(innerColorPacked.y) << 16);
    vec4 innerColor = unpackArgb(innerBits);
    vec4 outerColor = vertexColor;

    float startDeg = float(unpackU16(anglePacked.x)) / 65535.0 * 360.0;
    float endDeg = float(unpackU16(anglePacked.y)) / 65535.0 * 360.0;

    vec2 d = guiPos - ringCenter;
    float dist = length(d);

    // Match the original v1.0.0 shader behavior:
    // - outer clip uses maxR (fill)
    // - gradient uses full outerRadius
    float aa = max(0.0001, smoothPixels);
    float inRadius = smoothstep(innerRadius, innerRadius + aa, dist)
            * (1.0 - smoothstep(maxR - aa, maxR, dist));
    if (inRadius <= 0.0) {
        discard;
    }

    // Angle range: match Java angle convention (atan(d.x, -d.y)).
    float angle = atan2_wrap(d.x, -d.y);
    float startRad = radians(startDeg);
    float endRad = radians(endDeg);

    float total = endRad - startRad;
    if (total <= 0.0) {
        total += PI2;
    }

    float rel = mod(angle - startRad + PI2, PI2);

    float inAngle;
    if (total >= PI2 - 0.0001) {
        inAngle = 1.0;
    } else {
        // The legacy shader uses uSmooth (in UV units) directly as the angular AA width.
        float uSmooth = smoothPixels / max(0.0001, 2.0 * outerRadius);
        inAngle = smoothstep(0.0, uSmooth, rel)
                * (1.0 - smoothstep(total - uSmooth, total, rel));
    }
    if (inAngle <= 0.0) {
        discard;
    }

    float t = clamp((dist - innerRadius) / max(0.0001, outerRadius - innerRadius), 0.0, 1.0);
    vec4 color = mix(innerColor, outerColor, t) * ColorModulator;
    color.a *= (inRadius * inAngle);

    if (color.a <= 0.0) {
        discard;
    }
    fragColor = color;
}
