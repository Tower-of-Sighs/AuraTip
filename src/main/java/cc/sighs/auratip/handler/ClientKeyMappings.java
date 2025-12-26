package cc.sighs.auratip.handler;

import cc.sighs.auratip.AuraTip;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = AuraTip.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
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

