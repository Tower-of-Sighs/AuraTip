package cc.sighs.auratip.handler;

import cc.sighs.auratip.client.TipClient;
import cc.sighs.auratip.client.render.RadialMenuOverlay;
import cc.sighs.auratip.client.render.TipOverlay;
import cc.sighs.oelib.event.Subscribe;
import cc.sighs.oelib.event.events.TickEvent;
import net.minecraft.client.Minecraft;

public class ClientEventHandler {

    @Subscribe
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
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
