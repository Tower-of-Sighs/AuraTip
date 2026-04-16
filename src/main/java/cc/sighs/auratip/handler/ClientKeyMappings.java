package cc.sighs.auratip.handler;

import cc.sighs.auratip.dev.DevEnvironment;
import cc.sighs.oelib.registry.extra.KeyMappingRegister;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public class ClientKeyMappings {
    private static final String CATEGORY = "key.categories.auratip";

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

    public static void register() {
        KeyMappingRegister.register(CLOSE_TIP);

        if (DevEnvironment.isDev()) {
            KeyMappingRegister.register(DEV_OPEN_DATAPACK_MENU);
            KeyMappingRegister.register(DEV_OPEN_JAVA_MENU);
            KeyMappingRegister.register(DEV_OPEN_KJS_MENU);
            KeyMappingRegister.register(DEV_ENQUEUE_CLIENT_TIP);
            KeyMappingRegister.register(DEV_TRIGGER_SHOWTIP);
        }
    }
}
