package cc.sighs.auratip.client.render;

import cc.sighs.auratip.client.TipClient;
import cc.sighs.auratip.data.TipData;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public final class TipOverlay {
    public static final TipOverlay INSTANCE = new TipOverlay();

    private TipData tip;
    private List<TipData.Page> pages;
    private int themeColor;
    private int currentPage;
    private int remainingTicks;
    private int maxDuration;
    private float animationProgress;
    private boolean closing;
    private boolean hoveringInteractiveArea;
    private int panelWidth;
    private int panelHeight;
    private int panelX;
    private int panelY;
    private InputConstants.Key closeKey;

    private TipOverlay() {
    }

    public boolean isActive() {
        return tip != null;
    }

    public void show(TipData data) {
        this.tip = data;
        this.pages = data.pages().stream()
                .sorted(Comparator.comparingInt(TipData.Page::pageIndex))
                .toList();
        this.themeColor = parseColor(data.visualSettings().themeColor());
        this.currentPage = 0;
        this.maxDuration = data.behavior().defaultDuration();
        this.remainingTicks = this.maxDuration;
        this.animationProgress = 0.0f;
        this.closing = false;
        this.hoveringInteractiveArea = false;
        this.panelWidth = data.visualSettings().width();
        this.panelHeight = data.visualSettings().height();
        this.closeKey = null;
        Optional<String> keyId = data.behavior().closableByKey();
        if (keyId.isPresent()) {
            try {
                this.closeKey = InputConstants.getKey(keyId.get());
            } catch (Exception ignored) {
                this.closeKey = null;
            }
        }
    }

    public void tick(int screenWidth, int screenHeight) {
        if (tip == null) {
            return;
        }
        this.panelX = (screenWidth - panelWidth) / 2;
        this.panelY = (screenHeight - panelHeight) / 2;

        if (closing) {
            animationProgress = Mth.clamp(animationProgress - 0.15f, 0.0f, 1.0f);
            if (animationProgress <= 0.0f) {
                tip = null;
                TipClient.onTipClosed();
            }
            return;
        }

        animationProgress = Mth.clamp(animationProgress + 0.08f, 0.0f, 1.0f);

        if (maxDuration > 0 && remainingTicks > 0) {
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

        if (tip.visualSettings().blurBackground()) {
            renderBlurredBackground(graphics);
        }

        float progress = animationProgress;
        if (closing) {
            progress = Mth.clamp(progress - partialTick * 0.15f, 0.0f, 1.0f);
        } else {
            progress = Mth.clamp(progress + partialTick * 0.08f, 0.0f, 1.0f);
        }

        int x = panelX;
        int y = panelY;
        int w = panelWidth;
        int h = panelHeight;

        int slideOffset = (int) (18 * (1.0f - progress));
        int alpha = (int) (progress * 255.0f);
        int backgroundColor = (alpha << 24) | (themeColor & 0x00FFFFFF);
        int shadowColor = (alpha << 24) | 0x000000;

        graphics.fill(x + 2, y + slideOffset + 2, x + w + 2, y + h + slideOffset + 2, shadowColor);
        graphics.fill(x, y + slideOffset, x + w, y + h + slideOffset, backgroundColor);

        TipData.Page page = pages.get(currentPage);

        int contentX = x + 12;
        int contentY = y + 12 + slideOffset;

        if (page.title().isPresent()) {
            contentY = drawTextElement(graphics, page.title().get(), contentX, contentY);
        }
        if (page.subtitle().isPresent()) {
            contentY = drawTextElement(graphics, page.subtitle().get(), contentX, contentY + 4);
        }
        if (page.content().isPresent()) {
            contentY = drawTextElement(graphics, page.content().get(), contentX, contentY + 8);
        }

        if (page.image().isPresent()) {
            drawImage(graphics, page.image().get(), x, y + slideOffset, w);
        }

        int closeSize = 10;
        int closeX = x + w - closeSize - 4;
        int closeY = y + 4 + slideOffset;
        graphics.drawString(Minecraft.getInstance().font, "X", closeX, closeY, 0xFFFFFFFF);

        int indicatorY = y + h - 12 + slideOffset;
        String pageInfo = (currentPage + 1) + "/" + pages.size();
        int pageInfoWidth = Minecraft.getInstance().font.width(pageInfo);
        graphics.drawString(Minecraft.getInstance().font, pageInfo, x + (w - pageInfoWidth) / 2, indicatorY, 0xFFFFFFFF);

        if (tip.behavior().allowPaging() && pages.size() > 1) {
            String left = "<";
            String right = ">";
            graphics.drawString(Minecraft.getInstance().font, left, x + 8, indicatorY, 0xFFFFFFFF);
            graphics.drawString(Minecraft.getInstance().font, right, x + w - 8 - Minecraft.getInstance().font.width(right), indicatorY, 0xFFFFFFFF);
        }

        if (mouseX >= x && mouseX <= x + w && mouseY >= y + slideOffset && mouseY <= y + h + slideOffset) {
            hoveringInteractiveArea = true;
        }
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
        int slideOffset = (int) (18 * (1.0f - animationProgress));
        int x = panelX;
        int y = panelY + slideOffset;
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
        animationProgress = 0.0f;
        hoveringInteractiveArea = false;
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

    private int drawTextElement(GuiGraphics graphics, TipData.TextElement element, int x, int y) {
        float scale = element.scale();
        Component text = element.text();
        int lineSpacing = element.lineSpacing();

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0);
        graphics.pose().scale(scale, scale, 1.0f);
        graphics.drawString(Minecraft.getInstance().font, text, 0, 0, 0xFFFFFFFF);
        graphics.pose().popPose();

        int lineHeight = Minecraft.getInstance().font.lineHeight + lineSpacing;
        return y + (int) (lineHeight * scale);
    }

    private void drawImage(GuiGraphics graphics, TipData.ImageElement image, int panelX, int panelY, int panelWidth) {
        ResourceLocation texture = new ResourceLocation(image.path());
        int[] size = image.size();
        int imgW = size.length > 0 ? size[0] : 64;
        int imgH = size.length > 1 ? size[1] : 64;

        int x = panelX + (panelWidth - imgW) / 2;
        int y = panelY + 8;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);

        graphics.blit(texture, x, y, 0, 0, imgW, imgH, imgW, imgH);
    }

    private void startClosing() {
        if (!closing) {
            closing = true;
        }
    }

    private static int parseColor(String hex) {
        if (hex == null || hex.isBlank()) {
            return 0xA0D8EF;
        }
        String value = hex.startsWith("#") ? hex.substring(1) : hex;
        try {
            int rgb = Integer.parseInt(value, 16);
            if (value.length() <= 6) {
                return rgb;
            }
            return rgb & 0x00FFFFFF;
        } catch (NumberFormatException e) {
            return 0xA0D8EF;
        }
    }

    private void renderBlurredBackground(GuiGraphics graphics) {
        graphics.fill(0, 0, Minecraft.getInstance().getWindow().getGuiScaledWidth(), Minecraft.getInstance().getWindow().getGuiScaledHeight(), 0x40000000);
    }
}

