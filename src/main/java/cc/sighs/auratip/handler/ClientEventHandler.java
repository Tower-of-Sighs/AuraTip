package cc.sighs.auratip.handler;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.client.RadialMenuClient;
import cc.sighs.auratip.client.TipClient;
import cc.sighs.auratip.client.render.RadialMenuOverlay;
import cc.sighs.auratip.client.render.TipOverlay;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = AuraTip.MOD_ID, value = Dist.CLIENT)
public class ClientEventHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        while (ClientKeyMappings.OPEN_RADIAL.consumeClick()) {
            RadialMenuClient.openMenu();
        }

        while (ClientKeyMappings.CLOSE_TIP.consumeClick()) {
            TipClient.closeCurrentTip();
        }

        var mc = Minecraft.getInstance();
        int width = mc.getWindow().getGuiScaledWidth();
        int height = mc.getWindow().getGuiScaledHeight();

        TipOverlay.INSTANCE.tick(width, height);
        RadialMenuOverlay.INSTANCE.tick();
    }
}
