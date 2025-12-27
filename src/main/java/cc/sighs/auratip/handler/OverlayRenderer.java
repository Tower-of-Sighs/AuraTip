package cc.sighs.auratip.handler;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.client.render.RadialMenuOverlay;
import cc.sighs.auratip.client.render.TipOverlay;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AuraTip.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
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
        pose.translate(0, 0, 500);

        int width = event.getWindow().getGuiScaledWidth();
        int height = event.getWindow().getGuiScaledHeight();

        double mouseX = minecraft.mouseHandler.xpos() * (double) width / (double) event.getWindow().getScreenWidth();
        double mouseY = minecraft.mouseHandler.ypos() * (double) height / (double) event.getWindow().getScreenHeight();

        if (TipOverlay.INSTANCE.isActive()) {
            TipOverlay.INSTANCE.render(event.getGuiGraphics(), event.getPartialTick(), (int) mouseX, (int) mouseY, width, height);
        }

        if (RadialMenuOverlay.INSTANCE.isActive()) {
            RadialMenuOverlay.INSTANCE.render(event.getGuiGraphics(), event.getPartialTick(), (int) mouseX, (int) mouseY, width, height);
        }
        pose.popPose();
    }
}

