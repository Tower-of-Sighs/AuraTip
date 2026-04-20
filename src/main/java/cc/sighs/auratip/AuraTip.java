package cc.sighs.auratip;

import cc.sighs.auratip.command.AuraTipEditorCommand;
import cc.sighs.auratip.command.ShowTipCommand;
import cc.sighs.auratip.data.RadialMenuData;
import cc.sighs.auratip.data.TipData;
import cc.sighs.auratip.dev.DevEnvironment;
import cc.sighs.auratip.dev.DevJavaApiSamples;
import cc.sighs.oelib.data.DataRegistry;
import cc.sighs.oelib.network.api.NetworkAutoRegistration;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(AuraTip.MOD_ID)
public class AuraTip {
    public static final String MOD_ID = "auratip";
    public static final Logger LOGGER = LogUtils.getLogger();

    public AuraTip(IEventBus modEventBus, ModContainer modContainer) {
        DataRegistry.register(TipData.class, TipData.CODEC);
        DataRegistry.register(RadialMenuData.class, RadialMenuData.CODEC);
        NetworkAutoRegistration.registerBasePackage("cc.sighs.auratip.network");
        ShowTipCommand.register();
        AuraTipEditorCommand.register();

        if (DevEnvironment.isDev()) {
            DevJavaApiSamples.initCommon();
        }
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static String formattedMod(String path) {
        return path.formatted(MOD_ID);
    }

    public static boolean isPresentResource(Identifier identifier) {
        return Minecraft.getInstance().getResourceManager().getResource(identifier).isPresent();
    }

    private static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}
