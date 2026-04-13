package cc.sighs.auratip.handler;

import cc.sighs.oelib.registry.extra.KeyMappingRegister;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
public class ClientKeyMappings {
    private static final String CATEGORY = "key.categories.auratip";

    public static final KeyMapping OPEN_RADIAL = new KeyMapping(
            "key.auratip.open_radial",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_R,
            CATEGORY
    );

    public static final KeyMapping CLOSE_TIP = new KeyMapping(
            "key.auratip.close_tip",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_DELETE,
            CATEGORY
    );

    public static void register() {
        KeyMappingRegister.register(OPEN_RADIAL);
        KeyMappingRegister.register(CLOSE_TIP);
    }
}
