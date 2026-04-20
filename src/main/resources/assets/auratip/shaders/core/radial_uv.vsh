#version 330

// Copied layout from minecraft:dynamictransforms.glsl and minecraft:projection.glsl
layout(std140) uniform DynamicTransforms {
    mat4 ModelViewMat;
    vec4 ColorModulator;
    vec3 ModelOffset;
    mat4 TextureMat;
};
layout(std140) uniform Projection {
    mat4 ProjMat;
};

in vec3 Position;
in vec4 Color;
in vec2 UV0;     // center (cx, cy) in GUI coordinates
in ivec2 UV1;    // packed inner color ARGB (low16, high16)
in ivec2 UV2;    // packed start/end angle degrees (u16, u16) scaled to [0..65535]
in vec3 Normal;  // packed parameters: (fill, smooth, unused) in [-1..1]
in float LineWidth; // packed radii (inner, outer) as two u16 in float bits

out vec4 vertexColor;
out vec2 guiPos;
out vec2 ringCenter;
flat out ivec2 innerColorPacked;
flat out ivec2 anglePacked;
flat out uint radiiBits;
flat out vec3 packedParams;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    vertexColor = Color;
    guiPos = Position.xy;
    ringCenter = UV0;
    innerColorPacked = UV1;
    anglePacked = UV2;
    radiiBits = floatBitsToUint(LineWidth);
    packedParams = Normal;
}
