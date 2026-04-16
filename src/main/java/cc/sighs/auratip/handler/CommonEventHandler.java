package cc.sighs.auratip.handler;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.api.tip.TipServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AuraTip.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class CommonEventHandler {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        TipServer.trigger(new ResourceLocation(AuraTip.MODID, "first_join_world"), player);
    }
}
