package cc.sighs.auratip.network;

import cc.sighs.auratip.AuraTip;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber(modid = AuraTip.MOD_ID)
public final class NetworkHandler {
    @SubscribeEvent
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(AuraTip.MOD_ID);
        //s2c
        registrar.playToClient(ShowTipsPacket.TYPE, ShowTipsPacket.CODEC, ShowTipsPacket::execute);
    }
}
