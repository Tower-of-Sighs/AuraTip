package cc.sighs.auratip.client.render;

import cc.sighs.auratip.handler.AuraShaders;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.util.Mth;

public class RingRenderer {

    private RingRenderer() {
    }

    public static void drawRing(GuiGraphics gg,
                                int cx, int cy,
                                float innerRadius, float outerRadius,
                                float startDeg, float endDeg,
                                int innerColor, int outerColor,
                                float smoothPixels) {
        drawRing(gg, cx, cy, innerRadius, outerRadius,
                startDeg, endDeg, innerColor, outerColor, smoothPixels, 1.0f);
    }

    public static void drawRing(GuiGraphics gg,
                                int cx, int cy,
                                float innerRadius, float outerRadius,
                                float startDeg, float endDeg,
                                int innerColor, int outerColor,
                                float smoothPixels, float fill) {
        if (outerRadius < 0.1f) {
            return;
        }
        var shader = AuraShaders.getRadialRing();
        if (shader == null) return;

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(() -> shader);

        float x1 = cx - outerRadius;
        float y1 = cy - outerRadius;
        float x2 = cx + outerRadius;
        float y2 = cy + outerRadius;

        float innerR = Mth.clamp(innerRadius / (2.0f * outerRadius), 0.0f, 0.5f);
        float outerR = 0.5f;

        shader.safeGetUniform("uInnerRadius").set(innerR);
        shader.safeGetUniform("uOuterRadius").set(outerR);
        shader.safeGetUniform("uStartAngle").set(startDeg);
        shader.safeGetUniform("uEndAngle").set(endDeg);
        shader.safeGetUniform("uSmooth").set(smoothPixels / (2.0f * outerRadius));
        shader.safeGetUniform("uFill").set(fill);

        setColor(shader, "uInnerColor", innerColor);
        setColor(shader, "uOuterColor", outerColor);

        var mat = gg.pose().last().pose();
        var buf = Tesselator.getInstance().getBuilder();
        buf.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buf.vertex(mat, x1, y1, 0).uv(0.0f, 0.0f).endVertex();
        buf.vertex(mat, x1, y2, 0).uv(0.0f, 1.0f).endVertex();
        buf.vertex(mat, x2, y2, 0).uv(1.0f, 1.0f).endVertex();
        buf.vertex(mat, x2, y1, 0).uv(1.0f, 0.0f).endVertex();
        BufferUploader.drawWithShader(buf.end());
    }

    private static void setColor(ShaderInstance shader, String name, int argb) {
        float a = (argb >>> 24 & 255) / 255.0f;
        float r = (argb >>> 16 & 255) / 255.0f;
        float g = (argb >>> 8 & 255) / 255.0f;
        float b = (argb & 255) / 255.0f;
        shader.safeGetUniform(name).set(r, g, b, a);
    }
}