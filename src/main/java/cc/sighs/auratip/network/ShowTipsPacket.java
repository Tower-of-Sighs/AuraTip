package cc.sighs.auratip.network;

import cc.sighs.auratip.client.TipClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public record ShowTipsPacket(List<String> tipIds) {
    public static void encode(ShowTipsPacket packet, FriendlyByteBuf buf) {
        buf.writeCollection(packet.tipIds, FriendlyByteBuf::writeUtf);
    }

    public static ShowTipsPacket decode(FriendlyByteBuf buf) {
        var ids = buf.readList(FriendlyByteBuf::readUtf);
        return new ShowTipsPacket(new ArrayList<>(ids));
    }

    public static void handle(ShowTipsPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> TipClient.enqueueTipsById(packet.tipIds));
        ctx.get().setPacketHandled(true);
    }
}

