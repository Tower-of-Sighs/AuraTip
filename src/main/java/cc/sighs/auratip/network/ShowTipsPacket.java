package cc.sighs.auratip.network;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.client.TipClient;
import cc.sighs.auratip.data.TipData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public record ShowTipsPacket(List<TipData> tips, Map<String, String> variables) {
    public static void encode(ShowTipsPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.tips.size());
        for (TipData tip : packet.tips) {
            CompoundTag tag = (CompoundTag) TipData.CODEC.encodeStart(NbtOps.INSTANCE, tip)
                    .getOrThrow(false, message -> AuraTip.LOGGER.error("[AuraTip] Failed to encode TipData for network: {}", message));
            buf.writeNbt(tag);
        }
        buf.writeMap(packet.variables, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeUtf);
    }

    public static ShowTipsPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<TipData> tips = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            CompoundTag tag = buf.readNbt();
            if (tag == null) {
                continue;
            }
            TipData tip = TipData.CODEC.parse(NbtOps.INSTANCE, tag)
                    .getOrThrow(false, message -> AuraTip.LOGGER.error("[AuraTip] Failed to decode TipData from network: {}", message));
            tips.add(tip);
        }
        var vars = buf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readUtf);
        return new ShowTipsPacket(new ArrayList<>(tips), new HashMap<>(vars));
    }

    public static void handle(ShowTipsPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> TipClient.enqueueTips(packet.tips, packet.variables));
        ctx.get().setPacketHandled(true);
    }
}
