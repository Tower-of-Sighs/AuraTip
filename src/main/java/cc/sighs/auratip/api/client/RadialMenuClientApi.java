package cc.sighs.auratip.api.client;

import cc.sighs.auratip.client.RadialMenuClient;
import net.minecraft.resources.ResourceLocation;

/**
 * Client-side helpers for interacting with AuraTip's radial menu UI.
 * <p>
 * This class must not be referenced from dedicated-server-only code paths.
 */
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
