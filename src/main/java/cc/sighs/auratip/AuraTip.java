package cc.sighs.auratip;

import cc.sighs.auratip.command.ShowTipCommand;
import cc.sighs.auratip.data.RadialMenuData;
import cc.sighs.auratip.data.TipData;
import cc.sighs.auratip.dev.DevEnvironment;
import cc.sighs.auratip.dev.DevJavaApiSamples;
import cc.sighs.oelib.data.DataRegistry;
import cc.sighs.oelib.event.EventAutoRegistration;
import cc.sighs.oelib.network.api.NetworkAutoRegistration;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(AuraTip.MODID)
public class AuraTip {
    public static final String MODID = "auratip";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AuraTip() {
        DataRegistry.register(TipData.class, TipData.CODEC);
        DataRegistry.register(RadialMenuData.class, RadialMenuData.CODEC);
        NetworkAutoRegistration.registerBasePackage("cc.sighs.auratip.network");
        EventAutoRegistration.registerBasePackage("cc.sighs.auratip.handler");
        ShowTipCommand.register();

        if (DevEnvironment.isDev()) {
            DevJavaApiSamples.initCommon();
        }
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> AuraTipClient::init);
    }
}
