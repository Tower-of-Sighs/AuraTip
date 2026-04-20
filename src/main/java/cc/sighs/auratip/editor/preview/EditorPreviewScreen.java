package cc.sighs.auratip.editor.preview;

import net.minecraft.client.gui.GuiGraphicsExtractor;
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
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // Intentionally draw nothing.
        // The preview is rendered via HUD overlays (TipOverlay / RadialMenuOverlay).
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // Disable Screen's default background behavior (blur + menu texture).
        // We want a fully transparent "lock screen" so the HUD preview looks identical to in-game.
    }
}
