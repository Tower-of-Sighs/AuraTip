package cc.sighs.auratip.client.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.util.Mth;
import org.joml.Matrix3x2f;
import org.joml.Matrix3x2fc;
import org.jspecify.annotations.Nullable;

public class RingRenderer {

    private RingRenderer() {
    }

    public static void drawRing(GuiGraphicsExtractor gg,
                                int cx, int cy,
                                float innerRadius, float outerRadius,
                                float startDeg, float endDeg,
                                int innerColor, int outerColor,
                                float smoothPixels) {
        drawRing(gg, cx, cy, innerRadius, outerRadius,
                startDeg, endDeg, innerColor, outerColor, smoothPixels, 1.0f);
    }

    public static void drawRing(GuiGraphicsExtractor gg,
                                int cx, int cy,
                                float innerRadius, float outerRadius,
                                float startDeg, float endDeg,
                                int innerColor, int outerColor,
                                float smoothPixels, float fill) {
        if (outerRadius < 0.1f || fill <= 0.0f) {
            return;
        }

        float rInner = Math.max(0.0f, innerRadius);
        float rOuterFull = Math.max(rInner, outerRadius);

        float clampedFill = Mth.clamp(fill, 0.0f, 1.0f);
        float maxR = rInner + (rOuterFull - rInner) * clampedFill;
        if (maxR - rInner < 0.01f) {
            return;
        }

        float smooth = Math.max(0.0f, smoothPixels);

        Matrix3x2f pose = new Matrix3x2f(gg.pose());
        RenderPipeline pipeline = AuraTipRenderPipelines.RADIAL_RING;
        if (pipeline == null) {
            return;
        }
        gg.submitGuiElementRenderState(
                new RingRenderState(
                        pipeline,
                        TextureSetup.noTexture(),
                        pose,
                        cx,
                        cy,
                        rInner,
                        rOuterFull,
                        startDeg,
                        endDeg,
                        innerColor,
                        outerColor,
                        smooth,
                        clampedFill,
                        gg.peekScissorStack()
                )
        );
    }

    private record RingRenderState(
            RenderPipeline pipeline,
            TextureSetup textureSetup,
            Matrix3x2fc pose,
            int cx,
            int cy,
            float rInner,
            float rOuterFull,
            float startDeg,
            float endDeg,
            int innerColor,
            int outerColor,
            float smoothPixels,
            float fill,
            @Nullable ScreenRectangle scissorArea,
            @Nullable ScreenRectangle bounds
    ) implements GuiElementRenderState {
        private RingRenderState(
                RenderPipeline pipeline,
                TextureSetup textureSetup,
                Matrix3x2fc pose,
                int cx,
                int cy,
                float rInner,
                float rOuterFull,
                float startDeg,
                float endDeg,
                int innerColor,
                int outerColor,
                float smoothPixels,
                float fill,
                @Nullable ScreenRectangle scissorArea
        ) {
            this(
                    pipeline,
                    textureSetup,
                    pose,
                    cx,
                    cy,
                    rInner,
                    rOuterFull,
                    startDeg,
                    endDeg,
                    innerColor,
                    outerColor,
                    smoothPixels,
                    fill,
                    scissorArea,
                    getBounds(cx, cy, rOuterFull, smoothPixels, pose, scissorArea)
            );
        }

        @Override
        public void buildVertices(VertexConsumer vertexConsumer) {
            float left = this.cx - this.rOuterFull;
            float top = this.cy - this.rOuterFull;
            float right = this.cx + this.rOuterFull;
            float bottom = this.cy + this.rOuterFull;

            int innerLow = this.innerColor & 0xFFFF;
            int innerHigh = (this.innerColor >>> 16) & 0xFFFF;

            int startPacked = packAngleDeg(this.startDeg);
            int endPacked = packAngleDeg(this.endDeg);

            float radiiBits = Float.intBitsToFloat(packRadiiBits(this.rInner, this.rOuterFull));
            float fillNorm = Mth.clamp(this.fill, 0.0f, 1.0f) * 2.0f - 1.0f;
            float smoothNorm = packSmoothToNormal(this.smoothPixels);

            vertexConsumer.addVertexWith2DPose(this.pose, left, top)
                    .setColor(this.outerColor)
                    .setUv(this.cx, this.cy)
                    .setUv1(innerLow, innerHigh)
                    .setUv2(startPacked, endPacked)
                    .setNormal(fillNorm, smoothNorm, 0.0f)
                    .setLineWidth(radiiBits);
            vertexConsumer.addVertexWith2DPose(this.pose, left, bottom)
                    .setColor(this.outerColor)
                    .setUv(this.cx, this.cy)
                    .setUv1(innerLow, innerHigh)
                    .setUv2(startPacked, endPacked)
                    .setNormal(fillNorm, smoothNorm, 0.0f)
                    .setLineWidth(radiiBits);
            vertexConsumer.addVertexWith2DPose(this.pose, right, bottom)
                    .setColor(this.outerColor)
                    .setUv(this.cx, this.cy)
                    .setUv1(innerLow, innerHigh)
                    .setUv2(startPacked, endPacked)
                    .setNormal(fillNorm, smoothNorm, 0.0f)
                    .setLineWidth(radiiBits);
            vertexConsumer.addVertexWith2DPose(this.pose, right, top)
                    .setColor(this.outerColor)
                    .setUv(this.cx, this.cy)
                    .setUv1(innerLow, innerHigh)
                    .setUv2(startPacked, endPacked)
                    .setNormal(fillNorm, smoothNorm, 0.0f)
                    .setLineWidth(radiiBits);
        }
    }

    private static int packAngleDeg(float deg) {
        float d = deg;
        while (d < 0.0f) {
            d += 360.0f;
        }
        while (d > 360.0f) {
            d -= 360.0f;
        }
        int packed = Math.round(d / 360.0f * 65535.0f);
        return Mth.clamp(packed, 0, 65535);
    }

    private static int packRadiiBits(float innerPixels, float outerPixels) {
        int inner = Mth.clamp(Math.round(innerPixels * 16.0f), 0, 65535);
        int outer = Mth.clamp(Math.round(outerPixels * 16.0f), 0, 65535);
        return inner | (outer << 16);
    }

    private static final float SMOOTH_PACK_MAX = 8.0f;

    private static float packSmoothToNormal(float smoothPixels) {
        float t = Mth.clamp(smoothPixels / SMOOTH_PACK_MAX, 0.0f, 1.0f);
        return t * 2.0f - 1.0f;
    }

    private static @Nullable ScreenRectangle getBounds(
            int cx,
            int cy,
            float rOuter,
            float smoothPixels,
            Matrix3x2fc pose,
            @Nullable ScreenRectangle scissorArea
    ) {
        float r = Math.max(0.0f, rOuter + Math.max(0.0f, smoothPixels) + 1.0f);
        int x0 = (int) Math.floor(cx - r - 1.0f);
        int y0 = (int) Math.floor(cy - r - 1.0f);
        int x1 = (int) Math.ceil(cx + r + 1.0f);
        int y1 = (int) Math.ceil(cy + r + 1.0f);

        ScreenRectangle bounds = new ScreenRectangle(x0, y0, x1 - x0, y1 - y0).transformMaxBounds(pose);
        return scissorArea != null ? scissorArea.intersection(bounds) : bounds;
    }
}
