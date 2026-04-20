package cc.sighs.auratip;

import cc.sighs.auratip.client.render.AuraTipRenderPipelines;
import cc.sighs.auratip.compat.nekojs.NekoJSCompat;
import cc.sighs.auratip.dev.DevEnvironment;
import cc.sighs.auratip.dev.DevJavaApiSamplesClient;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = AuraTip.MOD_ID, dist = Dist.CLIENT)
public class AuraTipClient {
    public AuraTipClient(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(AuraTipRenderPipelines::onRegisterRenderPipelines);
        if (NekoJSCompat.isLoaded()) {
            modEventBus.addListener(NekoJSCompat::onClientSetup);
            modEventBus.addListener(NekoJSCompat::onAddClientReloadListeners);
        }
        if (DevEnvironment.isDev()) {
            DevJavaApiSamplesClient.initClient();
        }
    }
}
