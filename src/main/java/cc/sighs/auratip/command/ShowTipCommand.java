package cc.sighs.auratip.command;

import cc.sighs.auratip.compat.kubejs.tip.TipVariables;
import cc.sighs.auratip.data.trigger.TipTriggerManager;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class ShowTipCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("showtip")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();

                    updateVariables(player);

                    TipTriggerManager.trigger("SHOWTIP_COMMAND", player);

                    context.getSource().sendSuccess(() -> Component.literal("已尝试触发 Tip 演示案例"), true);

                    return 1;
                })
        );
    }

    private static void updateVariables(ServerPlayer player) {
        TipVariables.register("player", player.getScoreboardName());
        TipVariables.register("x", String.valueOf(player.getBlockX()));
        TipVariables.register("y", String.valueOf(player.getBlockY()));
        TipVariables.register("z", String.valueOf(player.getBlockZ()));
    }
}
