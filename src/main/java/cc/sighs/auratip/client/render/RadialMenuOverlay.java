package cc.sighs.auratip.client.render;

import cc.sighs.auratip.data.RadialMenuData;
import cc.sighs.auratip.data.action.Action;
import cc.sighs.auratip.data.action.ActionExecutor;
import cc.sighs.auratip.util.ColorUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public final class RadialMenuOverlay {

    public static final RadialMenuOverlay INSTANCE = new RadialMenuOverlay();

    private RadialMenuData menu;
    private List<RadialMenuData.Slot> slots;
    private int centerX;
    private int centerY;
    private float innerRadius;
    private float outerRadius;

    private int ringBaseRgb;
    private float ringAlphaFactor;

    private boolean closing;
    private long animationStartMs;
    private static final int OPEN_MS = 200;
    private static final int CLOSE_MS = 140;

    private int hoveredIndex = -1;
    private int activeIndex = -1;
    private float activeFill;
    private Minecraft mc;

    private RadialMenuOverlay() {
    }

    public boolean isActive() {
        return menu != null;
    }

    public void open(RadialMenuData menu, int screenWidth, int screenHeight, Minecraft mc) {
        this.mc = mc;
        this.menu = menu;
        this.slots = menu.slots();
        this.centerX = screenWidth / 2;
        this.centerY = screenHeight / 2;
        this.innerRadius = menu.menuSettings().innerRadius();
        this.outerRadius = menu.menuSettings().outerRadius();
        this.ringBaseRgb = 0x101622;
        this.ringAlphaFactor = 1.0f;
        menu.menuSettings().ringColor().ifPresent(color -> {
            int argb = ColorUtil.parseArgb(color);
            this.ringBaseRgb = argb & 0xFFFFFF;
            int a = (argb >>> 24) & 0xFF;
            if (a == 0) {
                a = 255;
            }
            this.ringAlphaFactor = a / 255.0f;
        });
        this.closing = false;
        this.animationStartMs = Util.getMillis();
        this.hoveredIndex = -1;
        this.activeIndex = -1;
        this.activeFill = 0.0f;

        mc.mouseHandler.releaseMouse();
    }

    public void close() {
        if (menu == null || closing) {
            return;
        }
        closing = true;
        animationStartMs = Util.getMillis();

        if (mc.screen == null) {
            mc.mouseHandler.grabMouse();
        }
    }

    public void tick() {
    }

    public void render(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (menu == null || slots == null || slots.isEmpty()) {
            return;
        }

        long now = Util.getMillis();
        int elapsed = (int) Math.max(0, now - animationStartMs);
        int duration = closing ? CLOSE_MS : OPEN_MS;
        float t = elapsed / (float) duration;
        t = Mth.clamp(t, 0.0f, 1.0f);
        float progress = closing ? 1.0f - t : t;

        if (closing && t >= 1.0f) {
            menu = null;
            slots = null;
            hoveredIndex = -1;
            activeIndex = -1;
            activeFill = 0.0f;
            return;
        }

        float eased = progress * progress;

        float ringInner = innerRadius * eased;
        float ringOuter = outerRadius * eased;

        int bgInner = ColorUtil.withAlpha(ringBaseRgb, (int) (70 * ringAlphaFactor * eased));
        int bgOuter = ColorUtil.withAlpha(ringBaseRgb, (int) (120 * ringAlphaFactor * eased));
        RingRenderer.drawRing(graphics, centerX, centerY, ringInner, ringOuter, 0.0f, 360.0f, bgInner, bgOuter, 1.5f);

        int count = slots.size();
        if (count == 0) {
            return;
        }

        double dx = mouseX - centerX;
        double dy = mouseY - centerY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        hoveredIndex = -1;

        if (dist >= ringInner && dist <= ringOuter && dist > 0.0) {
            double rad = Math.atan2(dx, -dy);
            double deg = Math.toDegrees(rad);
            if (deg < 0) {
                deg += 360.0;
            }

            double slice = 360.0 / count;
            double halfSlice = slice / 2.0;
            hoveredIndex = (int) ((deg + halfSlice) / slice) % count;
        }

        if (hoveredIndex != activeIndex) {
            activeIndex = hoveredIndex;
            activeFill = 0.0f;
        }

        float targetFill = activeIndex >= 0 ? 1.0f : 0.0f;
        activeFill += (targetFill - activeFill) * 0.25f;
        activeFill = Mth.clamp(activeFill, 0.0f, 1.0f);

        if (activeIndex >= 0 && activeFill > 0.01f) {
            RadialMenuData.Slot slot = slots.get(activeIndex);
            var colorOpt = slot.highlightColor();
            if (colorOpt.isPresent()) {
                int rgb = ColorUtil.parseRgb(colorOpt.get());
                int alpha1 = (int) (180 * activeFill);
                int innerHighlight = ColorUtil.withAlpha(rgb, (int) (70 * activeFill));
                int outerHighlight = ColorUtil.withAlpha(rgb, alpha1);

                double slice = 360.0 / count;
                float startDeg = (float) (-slice / 2.0 + activeIndex * slice);
                float endDeg = startDeg + (float) slice;

                RingRenderer.drawRing(graphics, centerX, centerY,
                        ringInner, ringOuter, startDeg, endDeg,
                        innerHighlight, outerHighlight, 1.5f, activeFill);
            }
        }

        double iconRadius = ringInner + (ringOuter - ringInner) * 0.65;
        for (int i = 0; i < count; i++) {
            RadialMenuData.Slot slot = slots.get(i);
            double slice = 2 * Math.PI / count;
            double angle = slice * i - Math.PI / 2;
            int iconX = centerX + (int) (iconRadius * Math.cos(angle));
            int iconY = centerY + (int) (iconRadius * Math.sin(angle));

            float scale = i == hoveredIndex ? 1.25f : 1.0f;
            drawIcon(graphics, slot.icon(), iconX, iconY, scale * eased);
        }

        if (hoveredIndex >= 0 && hoveredIndex < slots.size()) {
            RadialMenuData.Slot hovered = slots.get(hoveredIndex);
            Component text = hovered.text().orElseGet(() -> Component.translatable(hovered.name()));
            int textWidth = Minecraft.getInstance().font.width(text);
            int textY = centerY - Minecraft.getInstance().font.lineHeight / 2;
            graphics.drawString(Minecraft.getInstance().font, text,
                    centerX - textWidth / 2, textY, 0xFFFFFFFF);
        }
    }

    public boolean keyPressed(int keyCode) {
        if (menu == null) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (menu == null || button != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
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

    private void drawIcon(GuiGraphics graphics, ResourceLocation texture, int x, int y, float scale) {
        if (scale <= 0.0f) {
            return;
        }
        int size = (int) (24 * scale);
        int drawX = x - size / 2;
        int drawY = y - size / 2;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        graphics.blit(texture, drawX, drawY, 0, 0, size, size, size, size);
    }
}