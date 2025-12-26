package cc.sighs.auratip;

import cc.sighs.auratip.data.RadialMenuData;
import cc.sighs.auratip.data.TipData;
import cc.sighs.auratip.data.action.ActionRegistry;
import cc.sighs.auratip.network.NetworkHandler;
import com.mafuyu404.oelib.data.DataRegistry;
import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(AuraTip.MODID)
public class AuraTip {
    public static final String MODID = "auratip";
    private static final Logger LOGGER = LogUtils.getLogger();

    public AuraTip() {
        ActionRegistry.register();
        DataRegistry.register(TipData.class, TipData.CODEC);
        DataRegistry.register(RadialMenuData.class, RadialMenuData.CODEC);
        NetworkHandler.register();
    }
}
