package cc.sighs.auratip.network;

import cc.sighs.auratip.AuraTip;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class NetworkHandler {
    private static final String PROTOCOL_VERSION = "1";

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(AuraTip.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private NetworkHandler() {
    }

    public static void register() {
        int id = 0;
        CHANNEL.messageBuilder(ShowTipsPacket.class, id++)
                .decoder(ShowTipsPacket::decode)
                .encoder(ShowTipsPacket::encode)
                .consumerMainThread(ShowTipsPacket::handle)
                .add();
    }
}
