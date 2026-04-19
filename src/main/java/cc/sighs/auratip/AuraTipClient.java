package cc.sighs.auratip;

import cc.sighs.auratip.dev.DevEnvironment;
import cc.sighs.auratip.dev.DevJavaApiSamplesClient;
import cc.sighs.auratip.handler.AuraShaders;
import cc.sighs.auratip.handler.ClientKeyMappings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = AuraTip.MOD_ID, dist = Dist.CLIENT)
public class AuraTipClient {
    public AuraTipClient(IEventBus modEventBus, ModContainer modContainer) {
        AuraShaders.register();
        ClientKeyMappings.register();
        if (DevEnvironment.isDev()) {
            DevJavaApiSamplesClient.initClient();
        }
    }
}
