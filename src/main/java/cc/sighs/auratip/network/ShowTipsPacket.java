package cc.sighs.auratip.network;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.client.TipClient;
import cc.sighs.auratip.data.TipData;
import cc.sighs.oelib.network.api.INetworkContext;
import cc.sighs.oelib.network.api.INetworkPacket;
import cc.sighs.oelib.network.api.NetworkPacket;
import cc.sighs.oelib.network.api.Side;
import cc.sighs.oelib.network.serialization.JsonCodec;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Map;

@NetworkPacket(
        modId = AuraTip.MODID,
        id = "show_tips",
        side = Side.CLIENT,
        chunkThreshold = 8192
)
public record ShowTipsPacket(
        List<TipEntry> tips,
        Map<String, Component> variables
) implements INetworkPacket<ShowTipsPacket> {

    public record TipEntry(@JsonCodec(holder = TipData.class) TipData tip) {
    }

    @Override
    public void handle(INetworkContext context) {
        context.enqueueWork(() -> {
            var resolvedTips = tips == null ? List.<TipData>of() : tips.stream()
                    .map(TipEntry::tip)
                    .toList();
            TipClient.enqueueTips(resolvedTips, variables);
        });
    }
}
