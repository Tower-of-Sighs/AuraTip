package cc.sighs.auratip.editor.preview;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/**
 * Locks player interaction while the editor is active.
 * <p>
 * The actual preview is rendered via {@link cc.sighs.auratip.client.render.TipOverlay} in the HUD render event.
 */
public class EditorPreviewScreen extends Screen {

    private final Runnable onClose;

    public EditorPreviewScreen(Runnable onClose) {
        super(Component.literal("AuraTip Editor Preview"));
        this.onClose = onClose == null ? () -> {
        } : onClose;
    }

    @Override
    public boolean isPauseScreen() {
        // Freeze singleplayer world logic while keeping rendering active.
        return true;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }

    @Override
    public void onClose() {
        super.onClose();
        onClose.run();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        // Intentionally draw nothing.
        // The preview is rendered via HUD overlays (TipOverlay / RadialMenuOverlay).
    }

    @Override
    public void renderBackground(GuiGraphics graphics) {
        // No background dim.
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Let the screen handle ESC; otherwise don't swallow so key mappings can still work.
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        return super.mouseScrolled(mouseX, mouseY, delta);
    }
}
