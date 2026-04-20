package cc.sighs.auratip.client.render;

import cc.sighs.auratip.util.ColorUtil;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

public class PanelRenderer {
    public static void drawRoundedPanel(GuiGraphicsExtractor graphics,
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

        RenderPipeline pipeline = AuraTipRenderPipelines.ROUNDED_PANEL;
        if (pipeline == null) {
            return;
        }

        graphics.submitGuiElementRenderState(
                new RoundedPanelRenderState(
                        pipeline,
                        TextureSetup.noTexture(),
                        new org.joml.Matrix3x2f(graphics.pose()),
                        x,
                        y,
                        w,
                        h,
                        r,
                        baseColor,
                        graphics.peekScissorStack()
                )
        );
    }

    private record RoundedPanelRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            org.joml.Matrix3x2fc pose,
            int x,
            int y,
            int w,
            int h,
            float r,
            int color,
            @Nullable ScreenRectangle scissorArea,
            @Nullable ScreenRectangle bounds
    ) implements GuiElementRenderState {
        private RoundedPanelRenderState(
                RenderPipeline pipeline,
                TextureSetup textureSetup,
                org.joml.Matrix3x2fc pose,
                int x,
                int y,
                int w,
                int h,
                float r,
                int color,
                @Nullable ScreenRectangle scissorArea
        ) {
            this(
                    pipeline,
                    textureSetup,
                    pose,
                    x,
                    y,
                    w,
                    h,
                    r,
                    color,
                    scissorArea,
                    getBounds(x, y, w, h, r, pose, scissorArea)
            );
        }

        @Override
        public void buildVertices(VertexConsumer vertexConsumer) {
            int part = Math.max(3, (int) (this.r / 3.0f + 3.0f));
            double piece = Math.PI / 2.0 / (part + 1);

            float vx;
            float vy;

            vx = this.x - this.r;
            vy = this.y;
            vertexConsumer.addVertexWith2DPose(this.pose, vx, vy).setColor(this.color);
            vy = this.y + this.h;
            vertexConsumer.addVertexWith2DPose(this.pose, vx, vy).setColor(this.color);

            for (int i = 1; i <= part; i++) {
                float xOff = (float) (Math.cos(piece * i) * this.r);
                float yOff = (float) (Math.sin(piece * i) * this.r);
                vx = this.x - xOff;
                vy = this.y - yOff;
                vertexConsumer.addVertexWith2DPose(this.pose, vx, vy).setColor(this.color);
                vy = this.y + this.h + yOff;
                vertexConsumer.addVertexWith2DPose(this.pose, vx, vy).setColor(this.color);
            }

            vx = this.x;
            vy = this.y - this.r;
            vertexConsumer.addVertexWith2DPose(this.pose, vx, vy).setColor(this.color);
            vy = this.y + this.r + this.h;
            vertexConsumer.addVertexWith2DPose(this.pose, vx, vy).setColor(this.color);

            vx = this.x + this.w;
            vy = this.y - this.r;
            vertexConsumer.addVertexWith2DPose(this.pose, vx, vy).setColor(this.color);
            vy = this.y + this.r + this.h;
            vertexConsumer.addVertexWith2DPose(this.pose, vx, vy).setColor(this.color);

            for (int i = 1; i <= part; i++) {
                float yOff = (float) (Math.cos(piece * i) * this.r);
                float xOff = (float) (Math.sin(piece * i) * this.r);
                vx = this.x + this.w + xOff;
                vy = this.y - yOff;
                vertexConsumer.addVertexWith2DPose(this.pose, vx, vy).setColor(this.color);
                vy = this.y + this.h + yOff;
                vertexConsumer.addVertexWith2DPose(this.pose, vx, vy).setColor(this.color);
            }

            vx = this.x + this.w + this.r;
            vy = this.y;
            vertexConsumer.addVertexWith2DPose(this.pose, vx, vy).setColor(this.color);
            vy = this.y + this.h;
            vertexConsumer.addVertexWith2DPose(this.pose, vx, vy).setColor(this.color);
        }
    }

    private static ScreenRectangle getBounds(
            int x,
            int y,
            int w,
            int h,
            float r,
            Matrix3x2fc pose,
            @Nullable ScreenRectangle scissorArea
    ) {
        int x0 = (int) Math.floor(x - r - 1.0f);
        int y0 = (int) Math.floor(y - r - 1.0f);
        int x1 = (int) Math.ceil(x + w + r + 1.0f);
        int y1 = (int) Math.ceil(y + h + r + 1.0f);
        ScreenRectangle bounds = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
    }
}
