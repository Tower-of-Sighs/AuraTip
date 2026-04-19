package cc.sighs.auratip.network;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.client.TipClient;
import cc.sighs.auratip.data.TipData;
import cc.sighs.auratip.util.TextSerialization;
import cc.sighs.oelib.network.api.INetworkContext;
import cc.sighs.oelib.network.api.INetworkPacket;
import cc.sighs.oelib.network.api.NetworkPacket;
import cc.sighs.oelib.network.api.Side;
import cc.sighs.oelib.network.serialization.JsonCodec;
import cc.sighs.oelib.network.serialization.NetFieldCodec;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;

@NetworkPacket(
        modId = AuraTip.MOD_ID,
        id = "show_tips",
        side = Side.CLIENT,
        chunkThreshold = 8192
)
public record ShowTipsPacket(
        List<TipEntry> tips,
        @NetFieldCodec(holder = TextSerialization.class, field = "VARIABLES_CODEC")
        Map<String, Component> variables
) implements INetworkPacket<ShowTipsPacket> {

    @Override
    public void handle(INetworkContext context) {
        context.enqueueWork(() -> {
            var resolvedTips = tips == null ? List.<TipData>of() : tips.stream()
                    .map(TipEntry::tip)
                    .toList();
            TipClient.enqueueTips(resolvedTips, variables);
        });
    }

    public record TipEntry(@JsonCodec(holder = TipData.class) TipData tip) {
    }
}
