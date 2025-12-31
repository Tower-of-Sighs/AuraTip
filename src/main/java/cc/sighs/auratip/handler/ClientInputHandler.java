package cc.sighs.auratip.handler;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.client.render.RadialMenuOverlay;
import cc.sighs.auratip.client.render.TipOverlay;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = AuraTip.MOD_ID, value = Dist.CLIENT)
public class ClientInputHandler {

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }
        int key = event.getKey();
        if (TipOverlay.INSTANCE.isActive() && TipOverlay.INSTANCE.keyPressed(key)) {
            return;
        }
        if (RadialMenuOverlay.INSTANCE.isActive()) {
            RadialMenuOverlay.INSTANCE.keyPressed(key);
        }
    }

    @SubscribeEvent
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }
        var minecraft = Minecraft.getInstance();
        var window = minecraft.getWindow();
        int guiWidth = window.getGuiScaledWidth();
        int guiHeight = window.getGuiScaledHeight();
        double guiX = minecraft.mouseHandler.xpos() * guiWidth / window.getScreenWidth();
        double guiY = minecraft.mouseHandler.ypos() * guiHeight / window.getScreenHeight();

        if (TipOverlay.INSTANCE.isActive() && TipOverlay.INSTANCE.mouseClicked(guiX, guiY, event.getButton())) {
            event.setCanceled(true);
            return;
        }
        if (RadialMenuOverlay.INSTANCE.isActive() && RadialMenuOverlay.INSTANCE.mouseClicked(guiX, guiY, event.getButton())) {
            event.setCanceled(true);
        }
    }
}
