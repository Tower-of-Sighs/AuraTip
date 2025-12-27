package cc.sighs.auratip.client.render;

import cc.sighs.auratip.util.ColorUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;

public class PanelRenderer {
    public static void drawRoundedPanel(GuiGraphics graphics,
                                        int x, int y, int w, int h,
                                        int topColor, int bottomColor,
                                        float radiusPixels,
                                        float smoothPixels,
                                        float alphaFactor) {
        if (alphaFactor <= 0.0f || w <= 0 || h <= 0) {
            return;
        }

        int mid = ColorUtil.lerpColor(topColor, bottomColor, 0.5f);
        int baseColor = ColorUtil.multiplyAlpha(mid, alphaFactor);

        float r = Math.max(0.0f, radiusPixels);
        if (r <= 0.5f) {
            graphics.fill(x, y, x + w, y + h, baseColor);
            return;
        }

        float maxR = Math.min(w, h) / 2.0f;
        if (r > maxR) {
            r = maxR;
        }

        int part = Math.max(3, (int) (r / 3.0f + 3.0f));

        var pose = graphics.pose().last().pose();
        var builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        double piece = Math.PI / 2.0 / (part + 1);

        float vx;
        float vy;

        vx = x - r;
        vy = y;
        builder.vertex(pose, vx, vy, 0).color(baseColor).endVertex();
        vy = y + h;
        builder.vertex(pose, vx, vy, 0).color(baseColor).endVertex();
        for (int i = 1; i <= part; i++) {
            float xOff = (float) (Math.cos(piece * i) * r);
            float yOff = (float) (Math.sin(piece * i) * r);
            vx = x - xOff;
            vy = y - yOff;
            builder.vertex(pose, vx, vy, 0).color(baseColor).endVertex();
            vy = y + h + yOff;
            builder.vertex(pose, vx, vy, 0).color(baseColor).endVertex();
        }
        vx = x;
        vy = y - r;
        builder.vertex(pose, vx, vy, 0).color(baseColor).endVertex();
        vy = y + r + h;
        builder.vertex(pose, vx, vy, 0).color(baseColor).endVertex();
        vx = x + w;
        vy = y - r;
        builder.vertex(pose, vx, vy, 0).color(baseColor).endVertex();
        vy = y + r + h;
        builder.vertex(pose, vx, vy, 0).color(baseColor).endVertex();
        for (int i = 1; i <= part; i++) {
            float yOff = (float) (Math.cos(piece * i) * r);
            float xOff = (float) (Math.sin(piece * i) * r);
            vx = x + w + xOff;
            vy = y - yOff;
            builder.vertex(pose, vx, vy, 0).color(baseColor).endVertex();
            vy = y + h + yOff;
            builder.vertex(pose, vx, vy, 0).color(baseColor).endVertex();
        }
        vx = x + w + r;
        vy = y;
        builder.vertex(pose, vx, vy, 0).color(baseColor).endVertex();
        vy = y + h;
        builder.vertex(pose, vx, vy, 0).color(baseColor).endVertex();

        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        BufferUploader.drawWithShader(builder.end());
        RenderSystem.disableBlend();
    }
}
