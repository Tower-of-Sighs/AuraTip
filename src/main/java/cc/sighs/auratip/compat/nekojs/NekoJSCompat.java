package cc.sighs.auratip.compat.nekojs;

import cc.sighs.auratip.AuraTip;
import com.tkisor.nekojs.NekoJS;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;

public final class NekoJSCompat {
    public static final String NEKOJS_MOD_ID = NekoJS.MODID;

    private static final Identifier NEKOJS_CLIENT_RELOAD_LISTENER_ID = Identifier.fromNamespaceAndPath(NEKOJS_MOD_ID, "client_scripts_reload");
    private static final Identifier AURATIP_CLEAR_LISTENER_ID = Identifier.fromNamespaceAndPath(AuraTip.MOD_ID, "nekojs_clear");
    private static final Identifier AURATIP_REFRESH_LISTENER_ID = Identifier.fromNamespaceAndPath(AuraTip.MOD_ID, "nekojs_refresh");

    private NekoJSCompat() {
    }

    public static boolean isLoaded() {
        return ModList.get().isLoaded(NEKOJS_MOD_ID);
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        if (!isLoaded()) {
            return;
        }
        event.enqueueWork(NekoJSCompat::refreshClientRegistries);
    }

    public static void onAddClientReloadListeners(AddClientReloadListenersEvent event) {
        if (!isLoaded()) {
            return;
        }
        
        event.addListener(
                AURATIP_CLEAR_LISTENER_ID,
                (ResourceManagerReloadListener) _ -> clearClientRegistries()
        );

        event.addListener(
                AURATIP_REFRESH_LISTENER_ID,
                (ResourceManagerReloadListener) _ -> refreshClientRegistries()
        );
        
        if (event.getRegistry().containsKey(NEKOJS_CLIENT_RELOAD_LISTENER_ID)) {
            event.addDependency(AURATIP_CLEAR_LISTENER_ID, NEKOJS_CLIENT_RELOAD_LISTENER_ID);
            event.addDependency(NEKOJS_CLIENT_RELOAD_LISTENER_ID, AURATIP_REFRESH_LISTENER_ID);
        }
    }

    public static void clearClientRegistries() {
        try {
            NJSAuraTipPlugin.clearClientRegistries();
        } catch (Throwable t) {
            AuraTip.LOGGER.warn("NekoJS compat clear failed", t);
        }
    }

    public static void refreshClientRegistries() {
        try {
            NJSAuraTipPlugin.refreshClientRegistries();
        } catch (Throwable t) {
            AuraTip.LOGGER.warn("NekoJS compat refresh failed", t);
        }
    }
}
