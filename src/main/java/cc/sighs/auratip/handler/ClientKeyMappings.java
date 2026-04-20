package cc.sighs.auratip.handler;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.dev.DevEnvironment;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = AuraTip.MOD_ID, value = Dist.CLIENT)
public class ClientKeyMappings {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(AuraTip.id(AuraTip.MOD_ID));

    public static final KeyMapping CLOSE_TIP = new KeyMapping(
            "key.auratip.close_tip",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_DELETE,
            CATEGORY
    );

    public static final KeyMapping DEV_OPEN_DATAPACK_MENU = new KeyMapping(
            "key.auratip.dev.open_datapack_menu",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_F6,
            CATEGORY
    );

    public static final KeyMapping DEV_OPEN_JAVA_MENU = new KeyMapping(
            "key.auratip.dev.open_java_menu",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_F7,
            CATEGORY
    );

    public static final KeyMapping DEV_OPEN_KJS_MENU = new KeyMapping(
            "key.auratip.dev.open_kjs_menu",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_F8,
            CATEGORY
    );

    public static final KeyMapping DEV_ENQUEUE_CLIENT_TIP = new KeyMapping(
            "key.auratip.dev.enqueue_client_tip",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_F9,
            CATEGORY
    );

    public static final KeyMapping DEV_TRIGGER_SHOWTIP = new KeyMapping(
            "key.auratip.dev.trigger_showtip",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_F10,
            CATEGORY
    );

    @SubscribeEvent
    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(CLOSE_TIP);

        if (DevEnvironment.isDev()) {
            event.register(DEV_OPEN_DATAPACK_MENU);
            event.register(DEV_OPEN_JAVA_MENU);
            event.register(DEV_OPEN_KJS_MENU);
            event.register(DEV_ENQUEUE_CLIENT_TIP);
            event.register(DEV_TRIGGER_SHOWTIP);
        }
    }
}
