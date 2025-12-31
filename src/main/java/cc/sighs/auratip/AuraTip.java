package cc.sighs.auratip;

import cc.sighs.auratip.data.RadialMenuData;
import cc.sighs.auratip.data.TipData;
import com.mafuyu404.oelib.data.DataRegistry;
import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
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

    public AuraTip(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
        DataRegistry.register(TipData.class, TipData.CODEC);
        DataRegistry.register(RadialMenuData.class, RadialMenuData.CODEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static String formattedMod(String path) {
        return path.formatted(MOD_ID);
    }

    public static boolean isPresentResource(ResourceLocation resourceLocation) {
        return Minecraft.getInstance().getResourceManager().getResource(resourceLocation).isPresent();
    }

    private static boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }
}