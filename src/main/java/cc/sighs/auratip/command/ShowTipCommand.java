package cc.sighs.auratip.command;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.api.tip.TipServer;
import cc.sighs.auratip.dev.DevEnvironment;
import cc.sighs.oelib.registry.extra.CommandRegister;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public class ShowTipCommand {
    public static void register() {
        if (DevEnvironment.isDev()) {
            CommandRegister.registerServer(ShowTipCommand::registerCommand);
        }
    }

    public static void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher,
                                CommandBuildContext context,
                                Commands.CommandSelection environment) {

        dispatcher.register(Commands.literal("showtip")
                .requires(source -> source.hasPermission(2))
                .executes(ctx -> {
                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                    TipServer.trigger(
                            AuraTip.id("showtip_command"),
                            player,
                            buildVariables(player)
                    );

                    ctx.getSource().sendSuccess(() -> Component.literal("已尝试触发 Tip 演示案例"), true);

                    return 1;
                })
        );
    }

    private static Map<String, Object> buildVariables(ServerPlayer player) {
        return Map.of(
                "player", player.getDisplayName(),
                "x", player.getBlockX(),
                "y", player.getBlockY(),
                "z", player.getBlockZ()
        );
    }
}
