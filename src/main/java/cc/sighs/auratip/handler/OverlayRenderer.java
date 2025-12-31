package cc.sighs.auratip.handler;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.client.render.RadialMenuOverlay;
import cc.sighs.auratip.client.render.TipOverlay;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

@EventBusSubscriber(modid = AuraTip.MOD_ID, value = Dist.CLIENT)
public class OverlayRenderer {

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }

        var gg = event.getGuiGraphics();
        var pose = gg.pose();

        pose.pushPose();
        pose.translate(0, 0, 5000);

        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();

        double mouseX = minecraft.mouseHandler.xpos() * (double) width / (double) minecraft.getWindow().getScreenWidth();
        double mouseY = minecraft.mouseHandler.ypos() * (double) height / (double) minecraft.getWindow().getScreenHeight();

        if (TipOverlay.INSTANCE.isActive()) {
            TipOverlay.INSTANCE.render(event.getGuiGraphics(), event.getPartialTick().getRealtimeDeltaTicks(), (int) mouseX, (int) mouseY, width, height);
        }

        if (RadialMenuOverlay.INSTANCE.isActive()) {
            RadialMenuOverlay.INSTANCE.render(event.getGuiGraphics(), event.getPartialTick().getRealtimeDeltaTicks(), (int) mouseX, (int) mouseY, width, height);
        }
        pose.popPose();
    }
}

