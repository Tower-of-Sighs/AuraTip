package cc.sighs.auratip.network;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.client.TipClient;
import cc.sighs.oelib.network.api.INetworkContext;
import cc.sighs.oelib.network.api.INetworkPacket;
import cc.sighs.oelib.network.api.NetworkPacket;
import cc.sighs.oelib.network.api.Side;

@NetworkPacket(
        modId = AuraTip.MODID,
        id = "close_tip",
        side = Side.CLIENT
)
public record CloseTipPacket() implements INetworkPacket<CloseTipPacket> {

    @Override
    public void handle(INetworkContext context) {
        context.enqueueWork(TipClient::closeCurrentTip);
    }
}
