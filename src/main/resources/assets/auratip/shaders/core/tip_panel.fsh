#version 150

in vec2 texCoord0;

uniform vec4 ColorModulator;
uniform vec4 uTopColor;
uniform vec4 uBottomColor;
uniform float uRadius;
uniform float uSmooth;
uniform vec2 uInvSize;// (1/w, 1/h)

out vec4 fragColor;

void main() {
    vec2 uv = texCoord0;

    float r = clamp(uRadius, 0.0, 0.5);
    vec2 halfSize = vec2(0.5) - vec2(r);
    vec2 p = uv - vec2(0.5);
    vec2 q = abs(p) - halfSize;
    float dist = length(max(q, vec2(0.0))) - r;

    float d = dist;
    float fe = fwidth(d);
    float aa = max(uSmooth, fe * 1.5);
    float alpha = clamp(0.5 - d / aa, 0.0, 1.0);
    if (alpha <= 0.0) {
        discard;
    }

    float t = clamp(uv.y, 0.0, 1.0);
    vec4 baseColor = mix(uTopColor, uBottomColor, t) * ColorModulator;
    baseColor.a *= alpha;

    fragColor = baseColor;
}
