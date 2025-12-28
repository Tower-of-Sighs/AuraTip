package cc.sighs.auratip.network;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.client.TipClient;
import cc.sighs.auratip.data.TipData;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public record ShowTipsPacket(List<TipData> tips, Map<String, Component> variables) {

    public static void encode(ShowTipsPacket packet, FriendlyByteBuf buf) {
        buf.writeVarInt(packet.tips.size());
        for (TipData tip : packet.tips) {
            var json = TipData.CODEC.encodeStart(JsonOps.INSTANCE, tip)
                    .getOrThrow(false, message -> AuraTip.LOGGER.error("[AuraTip] Failed to encode TipData to JSON: {}", message));

            buf.writeUtf(json.toString());
        }

        buf.writeMap(packet.variables, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeComponent);

    }

    public static ShowTipsPacket decode(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        List<TipData> tips = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            try {
                String jsonStr = buf.readUtf();
                var element = JsonParser.parseString(jsonStr);

                TipData tip = TipData.CODEC.parse(JsonOps.INSTANCE, element)
                        .getOrThrow(false, message -> AuraTip.LOGGER.error("[AuraTip] Failed to decode TipData from JSON: {}", message));
                tips.add(tip);
            } catch (Exception e) {
                AuraTip.LOGGER.error("[AuraTip] Critical error decoding TipData: ", e);
            }
        }

        Map<String, Component> vars = buf.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readComponent);

        return new ShowTipsPacket(tips, vars);
    }

    public static void handle(ShowTipsPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> TipClient.enqueueTips(packet.tips, packet.variables));
        ctx.get().setPacketHandled(true);
    }
}