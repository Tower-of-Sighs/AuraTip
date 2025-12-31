package cc.sighs.auratip.handler;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.command.ShowTipCommand;
import cc.sighs.auratip.data.trigger.TipTriggerManager;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = AuraTip.MOD_ID)
public class CommonEventHandler {
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        TipTriggerManager.trigger("FIRST_JOIN_WORLD", player);
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        ShowTipCommand.register(event.getDispatcher());
    }
}
