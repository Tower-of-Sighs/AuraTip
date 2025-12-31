package cc.sighs.auratip.handler;

import cc.sighs.auratip.AuraTip;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = AuraTip.MOD_ID, value = Dist.CLIENT)
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

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        event.register(OPEN_RADIAL);
        event.register(CLOSE_TIP);
    }
}

