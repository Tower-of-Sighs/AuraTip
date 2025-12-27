package cc.sighs.auratip.compat.kubejs;

import cc.sighs.auratip.data.trigger.TipTriggerManager;
import net.minecraft.server.level.ServerPlayer;

public class TipTriggers {
    public static void trigger(String type, ServerPlayer player) {
        TipTriggerManager.trigger(type, player);
    }
}

