package cc.sighs.auratip.client.render;

import cc.sighs.auratip.data.RadialMenuData;
import cc.sighs.auratip.data.action.Action;
import cc.sighs.auratip.data.action.ActionExecutor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class RadialMenuOverlay {
    public static final RadialMenuOverlay INSTANCE = new RadialMenuOverlay();

    private RadialMenuData data;
    private List<RadialMenuData.Slot> slots;
    private int innerRadius;
    private int outerRadius;
    private int centerX;
    private int centerY;
    private float animationProgress;
    private boolean closing;
    private int hoveredIndex = -1;
    private int activeIndex = -1;
    private float activeFill;
    private long animationStartMs;
    private static final int OPEN_DURATION_MS = 200;
    private static final int CLOSE_DURATION_MS = 160;

    private RadialMenuOverlay() {
    }

    public boolean isActive() {
        return data != null;
    }

    public void open(RadialMenuData menu, int screenWidth, int screenHeight) {
        this.data = menu;
        this.slots = menu.slots();
        this.innerRadius = menu.menuSettings().innerRadius();
        this.outerRadius = menu.menuSettings().outerRadius();
        this.centerX = screenWidth / 2;
        this.centerY = screenHeight / 2;
        this.animationProgress = 0.0f;
        this.closing = false;
        this.hoveredIndex = -1;
        this.activeIndex = -1;
        this.activeFill = 0.0f;
        this.animationStartMs = Util.getMillis();

        var mc = Minecraft.getInstance();
        mc.mouseHandler.releaseMouse();
    }

    public void close() {
        if (data != null) {
            closing = true;
            animationStartMs = Util.getMillis();
        }
    }

    public void tick() {
    }

    public void render(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (data == null || slots == null || slots.isEmpty()) {
            return;
        }

        long now = Util.getMillis();
        int elapsed = (int) Math.max(0, now - animationStartMs);
        int duration = closing ? CLOSE_DURATION_MS : OPEN_DURATION_MS;
        float t = elapsed / (float) duration;
        t = Mth.clamp(t, 0.0f, 1.0f);
        animationProgress = closing ? 1.0f - t : t;

        if (closing && t >= 1.0f) {
            data = null;
            slots = null;
            hoveredIndex = -1;
            closing = false;
            activeIndex = -1;
            activeFill = 0.0f;
            return;
        }

        float progress = animationProgress;
        double smoothedInner = innerRadius * (0.6 + 0.4 * progress);
        double smoothedOuter = outerRadius * progress;

        drawRing(graphics, centerX, centerY, smoothedInner, smoothedOuter, (int) (180 * progress) << 24);

        double iconRadius = innerRadius + (outerRadius - innerRadius) * 0.6 * progress;

        int count = slots.size();
        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double distance = Math.sqrt(dx * dx + dy * dy);
        hoveredIndex = -1;

        if (distance >= smoothedInner && distance <= smoothedOuter && distance > 0.0) {
            double angle = Math.atan2(dy, dx);
            double normalized = angle / (2 * Math.PI) + 0.25;
            if (normalized < 0) {
                normalized += 1.0;
            }
            hoveredIndex = (int) (normalized * count) % count;
        }

        if (hoveredIndex != activeIndex) {
            activeIndex = hoveredIndex;
            activeFill = 0.0f;
        }

        float targetFill = activeIndex >= 0 ? 1.0f : 0.0f;
        activeFill += (targetFill - activeFill) * 0.25f;
        activeFill = Mth.clamp(activeFill, 0.0f, 1.0f);

        for (int i = 0; i < count; i++) {
            RadialMenuData.Slot slot = slots.get(i);
            double angle = 2 * Math.PI * i / count - Math.PI / 2;
            int iconX = centerX + (int) (iconRadius * Math.cos(angle));
            int iconY = centerY + (int) (iconRadius * Math.sin(angle));

            float scale = i == hoveredIndex ? 1.25f : 1.0f;
            drawSlotIcon(graphics, slot.icon(), iconX, iconY, scale, progress);
        }

        if (activeIndex >= 0 && activeFill > 0.01f) {
            RadialMenuData.Slot slot = slots.get(activeIndex);
            int rgb = parseColor(slot.highlightColor().orElse("#A0D8EF"));
            int alpha = (int) (140 * activeFill);
            int argb = (alpha << 24) | (rgb & 0xFFFFFF);
            drawSegment(graphics, centerX, centerY, smoothedInner, smoothedOuter, argb, activeIndex, count);
        }

        if (hoveredIndex >= 0 && hoveredIndex < slots.size()) {
            RadialMenuData.Slot hovered = slots.get(hoveredIndex);
            Component text = hovered.text().orElseGet(() -> Component.literal(hovered.name()));
            int textWidth = Minecraft.getInstance().font.width(text);
            int textY = centerY;
            graphics.drawString(Minecraft.getInstance().font, text, centerX - textWidth / 2, textY, 0xFFFFFFFF);
        }
    }

    public boolean keyPressed(int keyCode) {
        if (data == null) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (data == null || button != 0) {
            return false;
        }
        if (hoveredIndex < 0 || hoveredIndex >= slots.size()) {
            close();
            return true;
        }
        RadialMenuData.Slot slot = slots.get(hoveredIndex);
        Action action = slot.action();
        if (action != null) {
            action.accept(ActionExecutor.INSTANCE);
        }
        close();
        return true;
    }

    public void closeImmediately() {
        data = null;
        slots = null;
        hoveredIndex = -1;
        closing = false;
        animationProgress = 0.0f;
        activeIndex = -1;
        activeFill = 0.0f;
    }

    private void drawRing(GuiGraphics graphics, int cx, int cy, double inner, double outer, int baseColor) {
        if ((baseColor >>> 24) == 0) {
            return;
        }
        drawSector(graphics, cx, cy, inner, outer, 0.0, Math.PI * 2.0, baseColor, baseColor);
    }

    private void drawSlotIcon(GuiGraphics graphics, ResourceLocation icon, int x, int y, float scale, float progress) {
        int size = 24;
        float eased = progress * progress;
        float finalScale = scale * eased;
        int drawX = x - (int) (size * finalScale / 2);
        int drawY = y - (int) (size * finalScale / 2);
        int drawSize = (int) (size * finalScale);

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, icon);

        graphics.blit(icon, drawX, drawY, 0, 0, drawSize, drawSize, drawSize, drawSize);
    }

    private void drawSegment(GuiGraphics graphics, int cx, int cy, double inner, double outer, int color, int index, int total) {
        double start = 2 * Math.PI * index / total;
        double end = 2 * Math.PI * (index + 1) / total;
        int endA = color >>> 24;
        int endR = (color >> 16) & 255;
        int endG = (color >> 8) & 255;
        int endB = color & 255;
        int startColor = (0) | (endR << 16) | (endG << 8) | endB;
        drawSector(graphics, cx, cy, inner, outer, start, end, startColor, color);
    }

    private void drawSector(GuiGraphics graphics, int cx, int cy, double inner, double outer,
                            double startAngle, double endAngle, int startColor, int endColor) {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        int segments = 48;
        double start = startAngle;
        double end = endAngle;

        Matrix4f pose = graphics.pose().last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder builder = tesselator.getBuilder();

        builder.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        for (int i = 0; i <= segments; i++) {
            float t = i / (float) segments;
            double angle = Mth.lerp(t, start, end);

            int c = lerpARGB(startColor, endColor, t);

            int a = c >>> 24;
            int r = (c >> 16) & 255;
            int g = (c >> 8) & 255;
            int b = c & 255;

            float cos = (float) Math.cos(angle);
            float sin = (float) Math.sin(angle);

            float xOuter = cx + (float) (outer * cos);
            float yOuter = cy + (float) (outer * sin);
            float xInner = cx + (float) (inner * cos);
            float yInner = cy + (float) (inner * sin);

            builder.vertex(pose, xOuter, yOuter, 0).color(r, g, b, a).endVertex();
            builder.vertex(pose, xInner, yInner, 0).color(r, g, b, a).endVertex();
        }

        BufferUploader.drawWithShader(builder.end());
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private int parseColor(String hex) {
        if (hex == null || hex.isBlank()) {
            return 0xFFFFFF;
        }
        String value = hex.startsWith("#") ? hex.substring(1) : hex;
        try {
            return Integer.parseInt(value, 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            return 0xFFFFFF;
        }
    }

    private int lerpARGB(int a, int b, float t) {
        t = Mth.clamp(t, 0.0f, 1.0f);
        int aa = (int) (((a >>> 24) & 0xFF) * (1 - t) + ((b >>> 24) & 0xFF) * t);
        int rr = (int) (((a >> 16) & 0xFF) * (1 - t) + ((b >> 16) & 0xFF) * t);
        int gg = (int) (((a >> 8) & 0xFF) * (1 - t) + ((b >> 8) & 0xFF) * t);
        int bb = (int) (((a) & 0xFF) * (1 - t) + ((b) & 0xFF) * t);
        return (aa << 24) | (rr << 16) | (gg << 8) | bb;
    }
}
