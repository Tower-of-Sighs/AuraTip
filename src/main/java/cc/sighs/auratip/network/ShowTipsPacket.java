package cc.sighs.auratip.network;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.client.TipClient;
import cc.sighs.auratip.data.TipData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ShowTipsPacket(List<TipData> tips, Map<String, Component> variables) implements CustomPacketPayload {
    public static final Type<ShowTipsPacket> TYPE = new Type<>(AuraTip.id("show_tips_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ShowTipsPacket> CODEC = StreamCodec.composite(
            TipData.STREAM_CODEC.apply(ByteBufCodecs.list()),
            ShowTipsPacket::tips,
            ByteBufCodecs.map(
                    HashMap::new,
                    ByteBufCodecs.STRING_UTF8,
                    ComponentSerialization.STREAM_CODEC
            ),
            ShowTipsPacket::variables,
            ShowTipsPacket::new
    );

    public static void execute(ShowTipsPacket packet, IPayloadContext context) {
        TipClient.enqueueTips(packet.tips, packet.variables);
    }

    @Override
    public @NotNull CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}