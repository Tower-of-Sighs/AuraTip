package cc.sighs.auratip.client.render;

import cc.sighs.auratip.client.TipClient;
import cc.sighs.auratip.data.TipData;
import cc.sighs.auratip.data.TipData.VisualSettings;
import cc.sighs.auratip.data.animation.AnimationType;
import cc.sighs.auratip.data.animation.ha.HoverAnimation;
import cc.sighs.auratip.data.animation.ta.TransitionAnimation;
import cc.sighs.auratip.util.ColorUtil;
import cc.sighs.auratip.util.ComponentSerialization;
import cc.sighs.auratip.util.ResolveUtil;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class TipOverlay {
    public static final TipOverlay INSTANCE = new TipOverlay();

    private TipData tip;
    private List<TipData.Page> pages;
    private static final int OPEN_MS_BASE = 200;
    private static final int CLOSE_MS_BASE = 140;
    private int themeColor;
    private VisualSettings visualSettings;
    private int currentPage;
    private int remainingTicks;
    private int maxDuration;
    private TipData.VisualSettings.Background background;
    private boolean hasThemeColor;
    private TransitionAnimation transitionAnimation;
    private HoverAnimation hoverAnimation;
    private float hoverAnimationSpeed;
    private boolean hoverOnlyOnHover;
    private long hoverStartMs;
    private boolean hoverActive;
    private long animationStartMs;
    private int openDurationMs;
    private int closeDurationMs;
    private boolean closing;
    private boolean hoveringInteractiveArea;
    private int panelWidth;
    private int panelHeight;
    private int panelX;
    private int panelY;
    private TipData.Position position;
    private InputConstants.Key closeKey;
    private Map<String, Component> variables;

    private TipOverlay() {
    }

    public boolean isActive() {
        return tip != null;
    }

    public void show(TipData data, Map<String, Component> vars) {
        this.tip = data;
        this.pages = data.pages().stream()
                .sorted(Comparator.comparingInt(TipData.Page::pageIndex))
                .toList();
        this.visualSettings = data.visualSettings();
        this.background = visualSettings.background();
        this.hasThemeColor = false;
        this.themeColor = 0;
        Optional<String> theme = visualSettings.themeColor();
        if (theme.isPresent() && !theme.get().isBlank()) {
            this.themeColor = ColorUtil.parseArgb(theme.get());
            this.hasThemeColor = true;
        }
        this.currentPage = 0;
        this.maxDuration = data.behavior().defaultDuration();
        this.remainingTicks = this.maxDuration;
        this.transitionAnimation = AnimationType.resolve(visualSettings.animationStyle(), visualSettings.animationParams());
        float speed = visualSettings.animationSpeed();
        if (speed <= 0.0f) {
            speed = 1.0f;
        }
        this.openDurationMs = (int) (OPEN_MS_BASE / speed);
        this.closeDurationMs = (int) (CLOSE_MS_BASE / speed);
        this.animationStartMs = Util.getMillis();
        this.closing = false;
        this.hoveringInteractiveArea = false;
        this.panelWidth = visualSettings.width();
        this.panelHeight = visualSettings.height();
        this.position = visualSettings.position();
        this.variables = vars == null ? Map.of() : new HashMap<>(vars);
        this.closeKey = null;
        Optional<String> keyId = data.behavior().closableByKey();
        if (keyId.isPresent()) {
            try {
                this.closeKey = InputConstants.getKey(keyId.get());
            } catch (Exception ignored) {
                this.closeKey = null;
            }
        }

        this.hoverAnimation = AnimationType.resolveHover(visualSettings.hoverAnimationStyle(), visualSettings.hoverAnimationParams());
        float hoverSpeed = visualSettings.hoverAnimationSpeed();
        if (hoverSpeed <= 0.0f) {
            hoverSpeed = 1.0f;
        }
        this.hoverAnimationSpeed = hoverSpeed;
        this.hoverOnlyOnHover = visualSettings.hoverOnlyOnHover();
        this.hoverStartMs = -1L;
        this.hoverActive = false;
    }

    public void tick(int screenWidth, int screenHeight) {
        if (tip == null) {
            return;
        }

        updatePanelBounds(screenWidth, screenHeight);

        if (!closing && maxDuration > 0 && remainingTicks > 0) {
            boolean pause = tip.behavior().pauseTimerOnHover() && hoveringInteractiveArea;
            if (!pause) {
                remainingTicks--;
                if (remainingTicks <= 0) {
                    startClosing();
                }
            }
        }
    }

    public void render(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (tip == null) {
            return;
        }

        hoveringInteractiveArea = false;

        if (transitionAnimation == null) {
            transitionAnimation = AnimationType.resolve(null);
            animationStartMs = Util.getMillis();
            openDurationMs = OPEN_MS_BASE;
            closeDurationMs = CLOSE_MS_BASE;
        }

        if (hoverAnimation == null) {
            hoverAnimation = AnimationType.resolveHover(null);
        }

        long now = Util.getMillis();
        float eased = transitionAnimation.easedProgress(now, animationStartMs, closing, openDurationMs, closeDurationMs);

        if (closing && eased <= 0.001f) {
            tip = null;
            closing = false;
            hoveringInteractiveArea = false;
            variables = Map.of();
            TipClient.onTipClosed();
            return;
        }

        int x = panelX;
        int y = panelY;
        int w = panelWidth;
        int h = panelHeight;

        int drawX;
        int drawY;

        Optional<TipData.Position> from = visualSettings.animationFrom();
        Optional<TipData.Position> to = visualSettings.animationTo();
        if (from.isPresent()) {
            TipData.Position startPos = from.get();
            TipData.Position endPos = to.orElse(position);
            int[] start = resolvePanelPosition(startPos, screenWidth, screenHeight);
            int[] end = resolvePanelPosition(endPos, screenWidth, screenHeight);
            float fx = Mth.lerp(eased, start[0], end[0]);
            float fy = Mth.lerp(eased, start[1], end[1]);
            drawX = Mth.floor(fx);
            drawY = Mth.floor(fy);
        } else {
            int offsetX = transitionAnimation.offsetX(eased, panelWidth, panelHeight);
            int offsetY = transitionAnimation.offsetY(eased, panelWidth, panelHeight);
            drawX = x + offsetX;
            drawY = y + offsetY;
        }

        Minecraft mc = Minecraft.getInstance();
        boolean cursorVisible = mc.screen != null;
        boolean pointerInBaseArea = cursorVisible && mouseX >= drawX && mouseX <= drawX + w && mouseY >= drawY && mouseY <= drawY + h;

        boolean entryDone = !closing && eased >= 0.999f;
        if (!entryDone || closing) {
            hoverActive = false;
            hoverStartMs = -1L;
        } else {
            if (hoverOnlyOnHover) {
                if (pointerInBaseArea) {
                    if (!hoverActive) {
                        hoverActive = true;
                        hoverStartMs = now;
                    }
                } else {
                    hoverActive = false;
                }
            } else {
                if (!hoverActive) {
                    hoverActive = true;
                    hoverStartMs = now;
                }
            }
        }

        int hoverOffsetX = 0;
        int hoverOffsetY = 0;
        if (hoverActive && hoverAnimation != null && hoverStartMs >= 0L) {
            hoverOffsetX = hoverAnimation.offsetX(now, hoverStartMs, w, h, hoverAnimationSpeed);
            hoverOffsetY = hoverAnimation.offsetY(now, hoverStartMs, w, h, hoverAnimationSpeed);
        }

        drawX += hoverOffsetX;
        drawY += hoverOffsetY;

        renderPanelShadow(graphics, drawX, drawY, w, h, eased);
        renderPanelBackground(graphics, drawX, drawY, w, h, eased);

        TipData.Page page = pages.get(currentPage);

        int contentX = drawX + 12;
        int contentY = drawY + 12;

        if (page.title().isPresent()) {
            ComponentSerialization.TextElement title = page.title().get();
            contentY = drawTextElement(graphics, title, contentX, contentY);
            if (title.divider().isPresent()) {
                contentY = drawDivider(graphics, title.divider().get(), contentX, drawX + w - 12, contentY);
            }
        }
        if (page.subtitle().isPresent()) {
            ComponentSerialization.TextElement subtitle = page.subtitle().get();
            contentY = drawTextElement(graphics, subtitle, contentX, contentY + 4);
            if (page.title().isEmpty() && subtitle.divider().isPresent()) {
                contentY = drawDivider(graphics, subtitle.divider().get(), contentX, drawX + w - 12, contentY);
            }
        }
        if (page.content().isPresent()) {
            contentY = drawTextElement(graphics, page.content().get(), contentX, contentY + 8);
        }

        if (page.image().isPresent()) {
            drawImage(graphics, page.image().get(), drawX, drawY, w);
        }

        int closeSize = 10;
        int closeX = drawX + w - closeSize - 4;
        int closeY = drawY + 4;
        graphics.drawString(Minecraft.getInstance().font, "X", closeX, closeY, 0xFFFFFFFF);

        int indicatorY = drawY + h - 12;
        String pageInfo = (currentPage + 1) + "/" + pages.size();
        int pageInfoWidth = Minecraft.getInstance().font.width(pageInfo);
        graphics.drawString(Minecraft.getInstance().font, pageInfo, drawX + (w - pageInfoWidth) / 2, indicatorY, 0xFFFFFFFF);

        if (tip.behavior().allowPaging() && pages.size() > 1) {
            String left = "<";
            String right = ">";
            graphics.drawString(Minecraft.getInstance().font, left, drawX + 8, indicatorY, 0xFFFFFFFF);
            graphics.drawString(Minecraft.getInstance().font, right, drawX + w - 8 - Minecraft.getInstance().font.width(right), indicatorY, 0xFFFFFFFF);
        }

        hoveringInteractiveArea = cursorVisible && mouseX >= drawX && mouseX <= drawX + w && mouseY >= drawY && mouseY <= drawY + h;
    }

    public boolean keyPressed(int keyCode) {
        if (tip == null) {
            return false;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            startClosing();
            return true;
        }
        if (closeKey != null && keyCode == closeKey.getValue()) {
            startClosing();
            return true;
        }
        if (tip.behavior().allowPaging() && pages.size() > 1) {
            if (keyCode == GLFW.GLFW_KEY_LEFT) {
                previousPage();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_RIGHT) {
                nextPage();
                return true;
            }
        }
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (tip == null) {
            return false;
        }
        if (button != 0) {
            return false;
        }
        if (transitionAnimation == null) {
            transitionAnimation = AnimationType.resolve(null);
            animationStartMs = Util.getMillis();
            openDurationMs = OPEN_MS_BASE;
            closeDurationMs = CLOSE_MS_BASE;
        }
        long now = Util.getMillis();
        float eased = transitionAnimation.easedProgress(now, animationStartMs, closing, openDurationMs, closeDurationMs);

        int offsetX = transitionAnimation.offsetX(eased, panelWidth, panelHeight);
        int offsetY = transitionAnimation.offsetY(eased, panelWidth, panelHeight);

        int hoverOffsetX = 0;
        int hoverOffsetY = 0;
        if (hoverActive && hoverAnimation != null && hoverStartMs >= 0L) {
            hoverOffsetX = hoverAnimation.offsetX(now, hoverStartMs, panelWidth, panelHeight, hoverAnimationSpeed);
            hoverOffsetY = hoverAnimation.offsetY(now, hoverStartMs, panelWidth, panelHeight, hoverAnimationSpeed);
        }

        int x = panelX + offsetX + hoverOffsetX;
        int y = panelY + offsetY + hoverOffsetY;
        int w = panelWidth;

        int closeSize = 10;
        int closeX = x + w - closeSize - 4;
        int closeY = y + 4;
        if (mouseX >= closeX && mouseX <= closeX + closeSize * 2 && mouseY >= closeY && mouseY <= closeY + closeSize * 2) {
            startClosing();
            return true;
        }

        if (tip.behavior().allowPaging() && pages.size() > 1) {
            int indicatorY = y + panelHeight - 12;
            String left = "<";
            String right = ">";
            int leftWidth = Minecraft.getInstance().font.width(left);
            int rightWidth = Minecraft.getInstance().font.width(right);
            int leftX = x + 8;
            int rightX = x + w - 8 - rightWidth;

            if (mouseY >= indicatorY && mouseY <= indicatorY + Minecraft.getInstance().font.lineHeight) {
                if (mouseX >= leftX && mouseX <= leftX + leftWidth) {
                    previousPage();
                    return true;
                }
                if (mouseX >= rightX && mouseX <= rightX + rightWidth) {
                    nextPage();
                    return true;
                }
            }
        }
        return false;
    }

    public void closeImmediately() {
        tip = null;
        closing = false;
        hoveringInteractiveArea = false;
    }

    public void requestClose() {
        startClosing();
    }

    private void updatePanelBounds(int screenWidth, int screenHeight) {
        if (panelWidth <= 0 || panelHeight <= 0) {
            return;
        }
        TipData.Position pos = position;
        if (pos == null && visualSettings != null) {
            pos = visualSettings.position();
        }
        int[] resolved = resolvePanelPosition(pos, screenWidth, screenHeight);
        this.panelX = resolved[0];
        this.panelY = resolved[1];
    }

    private int[] resolvePanelPosition(TipData.Position pos, int screenWidth, int screenHeight) {
        if (pos != null && pos.absolute()) {
            return new int[]{pos.x(), pos.y()};
        }
        String preset = pos != null && pos.preset() != null ? pos.preset() : "CENTER";
        String normalized = preset.toUpperCase(Locale.ROOT);
        int margin = 16;
        int x;
        int y;

        switch (normalized) {
            case "TOP_LEFT" -> {
                x = margin;
                y = margin;
            }
            case "TOP_CENTER" -> {
                x = (screenWidth - panelWidth) / 2;
                y = margin;
            }
            case "TOP_RIGHT" -> {
                x = screenWidth - panelWidth - margin;
                y = margin;
            }
            case "LEFT_CENTER" -> {
                x = margin;
                y = (screenHeight - panelHeight) / 2;
            }
            case "RIGHT_CENTER" -> {
                x = screenWidth - panelWidth - margin;
                y = (screenHeight - panelHeight) / 2;
            }
            case "BOTTOM_LEFT" -> {
                x = margin;
                y = screenHeight - panelHeight - margin;
            }
            case "BOTTOM_CENTER" -> {
                x = (screenWidth - panelWidth) / 2;
                y = screenHeight - panelHeight - margin;
            }
            case "BOTTOM_RIGHT" -> {
                x = screenWidth - panelWidth - margin;
                y = screenHeight - panelHeight - margin;
            }
            default -> {
                x = (screenWidth - panelWidth) / 2;
                y = (screenHeight - panelHeight) / 2;
            }
        }
        return new int[]{x, y};
    }

    private void renderPanelShadow(GuiGraphics graphics, int x, int y, int w, int h, float eased) {
        int alpha = (int) (eased * 140.0f);
        if (alpha <= 0) {
            return;
        }
        int shadowColor = (alpha << 24);
//        graphics.fill(x + 2, y + 2, x + w + 2, y + h + 2, shadowColor);
    }

    private void renderPanelBackground(GuiGraphics graphics, int x, int y, int w, int h, float eased) {
        int radius = 0;
        VisualSettings.BackgroundType type = null;
        boolean rounded = true;

        if (background != null) {
            radius = Math.max(0, background.borderRadius());
            type = background.type();
            rounded = background.rounded();
        }

        float alphaFactor = eased;
        int alpha = (int) (alphaFactor * 255.0f);
        if (alpha <= 0) {
            return;
        }

        int topColor;
        int bottomColor;
        if (type == TipData.VisualSettings.BackgroundType.GRADIENT && background.colors() != null && !background.colors().isEmpty()) {
            var colors = background.colors();
            String fromHex = colors.get(0);
            String toHex = colors.get(colors.size() - 1);
            topColor = ColorUtil.parseArgb(fromHex);
            bottomColor = ColorUtil.parseArgb(toHex);
        } else if (type == TipData.VisualSettings.BackgroundType.SOLID && background.colors() != null && !background.colors().isEmpty()) {
            String hex = background.colors().get(0);
            topColor = ColorUtil.parseArgb(hex);
            bottomColor = topColor;
        } else {
            if (hasThemeColor) {
                topColor = themeColor;
                bottomColor = themeColor;
            } else {
                topColor = ColorUtil.parseArgb(null);
                bottomColor = topColor;
            }
        }

        float radiusPixels = rounded ? radius : 0.0f;
        PanelRenderer.drawRoundedPanel(graphics, x, y, w, h, topColor, bottomColor, radiusPixels, 2.0f, alpha / 255.0f);

        if (hasThemeColor) {
            int configuredWidth = visualSettings.stripeWidth();
            if (configuredWidth > 0) {
                int stripeBase = themeColor;
                int stripeColor = ColorUtil.multiplyAlpha(stripeBase, alphaFactor);
                int stripeTop = y + Math.round(radiusPixels);
                int stripeBottom = y + h - Math.round(radiusPixels);
                float lengthFactor = visualSettings.stripeLengthFactor();
                if (lengthFactor < 0.0f) {
                    lengthFactor = 0.0f;
                }
                if (lengthFactor > 1.0f) {
                    lengthFactor = 1.0f;
                }
                int available = stripeBottom - stripeTop;
                int actual = Math.round(available * lengthFactor);
                stripeBottom = stripeTop + actual;
                if (stripeBottom > stripeTop) {
                    graphics.fill(x, stripeTop, x + configuredWidth, stripeBottom, stripeColor);
                }
            }
        }
    }

    private void previousPage() {
        if (pages.size() <= 1) {
            return;
        }
        if (currentPage > 0) {
            currentPage--;
        } else {
            currentPage = pages.size() - 1;
        }
        if (maxDuration > 0) {
            remainingTicks = maxDuration;
        }
    }

    private void nextPage() {
        if (pages.size() <= 1) {
            return;
        }
        if (currentPage < pages.size() - 1) {
            currentPage++;
        } else {
            currentPage = 0;
        }
        if (maxDuration > 0) {
            remainingTicks = maxDuration;
        }
    }

    private int drawTextElement(GuiGraphics graphics, ComponentSerialization.TextElement element, int x, int y) {
        float scale = element.scale();
        var text = ResolveUtil.resolveVariables(element.text(), this.variables);
        int lineSpacing = element.lineSpacing();

        var font = Minecraft.getInstance().font;
        var lines = font.split(text, Integer.MAX_VALUE);
        if (lines.isEmpty()) {
            return y;
        }

        int baseLineHeight = font.lineHeight + lineSpacing;

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1.0f);

        int drawY = 0;
        for (var line : lines) {
            graphics.drawString(font, line, 0, drawY, 0xFFFFFFFF);
            drawY += baseLineHeight;
        }

        graphics.pose().popPose();

        return y + (int) (lines.size() * baseLineHeight * scale);
    }

    private int drawDivider(GuiGraphics graphics, ComponentSerialization.Divider divider, int leftX, int rightX, int y) {
        int thickness = divider.thickness();
        int marginTop = divider.marginTop();
        int marginBottom = divider.marginBottom();
        float length = divider.length();

        int lineY = y + marginTop;
        int x1 = leftX;
        int x2 = Math.max(leftX, rightX);
        int span = x2 - x1;
        if (span <= 0) {
            return y;
        }
        float clamped = Mth.clamp(length, 0.0f, 1.0f);
        int lineWidth = Math.max(1, (int) (span * clamped));
        int startX = x1;
        int endX = x1 + lineWidth;

        int argb;
        String colorHex = divider.color();
        if (colorHex != null && !colorHex.isBlank()) {
            argb = ColorUtil.parseArgb(colorHex);
        } else if (hasThemeColor) {
            argb = ColorUtil.multiplyAlpha(themeColor, 0.8f);
        } else {
            argb = 0x60FFFFFF;
        }

        graphics.fill(startX, lineY, endX, lineY + thickness, argb);
        return lineY + thickness + marginBottom;
    }

    private void drawImage(GuiGraphics graphics, TipData.ImageElement image, int panelX, int panelY, int panelWidth) {
        ResourceLocation texture = ResourceLocation.parse(image.path());
        int[] size = image.size();
        int imgW = size.length > 0 ? size[0] : 64;
        int imgH = size.length > 1 ? size[1] : 64;
        float scale = image.scale();
        if (scale > 0.0f && scale != 1.0f) {
            imgW = Math.max(1, Math.round(imgW * scale));
            imgH = Math.max(1, Math.round(imgH * scale));
        }
        int x;
        int y;

        TipData.Position pos = image.position();
        if (pos != null && pos.absolute()) {
            x = panelX + pos.x();
            y = panelY + pos.y();
        } else {
            String preset = pos != null && pos.preset() != null ? pos.preset() : "TOP_CENTER";
            String normalized = preset.toUpperCase(Locale.ROOT);
            switch (normalized) {
                case "TOP_LEFT" -> {
                    x = panelX;
                    y = panelY;
                }
                case "TOP_RIGHT" -> {
                    x = panelX + panelWidth - imgW;
                    y = panelY;
                }
                case "BOTTOM_LEFT" -> {
                    x = panelX;
                    y = panelY + panelHeight - imgH;
                }
                case "BOTTOM_RIGHT" -> {
                    x = panelX + panelWidth - imgW;
                    y = panelY + panelHeight - imgH;
                }
                case "BOTTOM_CENTER" -> {
                    x = panelX + (panelWidth - imgW) / 2;
                    y = panelY + panelHeight - imgH - 4;
                }
                case "CENTER", "MIDDLE" -> {
                    x = panelX + (panelWidth - imgW) / 2;
                    y = panelY + (panelHeight - imgH) / 2;
                }
                default -> {
                    x = panelX + (panelWidth - imgW) / 2;
                    y = panelY + 8;
                }
            }
        }

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);

        graphics.blit(texture, x, y, 0, 0, imgW, imgH, imgW, imgH);
    }

    private void startClosing() {
        if (!closing) {
            closing = true;
            animationStartMs = Util.getMillis();
        }
    }
}
