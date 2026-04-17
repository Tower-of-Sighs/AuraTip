package cc.sighs.auratip.network;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.editor.client.EditorClient;
import cc.sighs.oelib.network.api.INetworkContext;
import cc.sighs.oelib.network.api.INetworkPacket;
import cc.sighs.oelib.network.api.NetworkPacket;
import cc.sighs.oelib.network.api.Side;

@NetworkPacket(
        modId = AuraTip.MODID,
        id = "open_editor",
        side = Side.CLIENT
)
public record OpenEditorPacket(String mode) implements INetworkPacket<OpenEditorPacket> {

    @Override
    public void handle(INetworkContext context) {
        context.enqueueWork(() -> EditorClient.open(mode));
    }
}

