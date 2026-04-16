package cc.sighs.auratip.api.client;

import cc.sighs.auratip.client.RadialMenuClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Client-side helpers for interacting with AuraTip's radial menu UI.
 * <p>
 * This class is {@link OnlyIn} client and must not be referenced from dedicated-server-only code paths.
 */
@OnlyIn(Dist.CLIENT)
public final class RadialMenuClientApi {

    private RadialMenuClientApi() {
    }

    /**
     * Opens a specific radial menu by id.
     */
    public static void open(ResourceLocation menuId) {
        RadialMenuClient.openMenu(menuId);
    }
}
