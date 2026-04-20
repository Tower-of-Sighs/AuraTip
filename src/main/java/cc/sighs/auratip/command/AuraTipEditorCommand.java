package cc.sighs.auratip.command;

import cc.sighs.auratip.network.OpenEditorPacket;
import cc.sighs.oelib.registry.extra.CommandRegister;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.Permission;
import net.minecraft.server.permissions.PermissionLevel;

/**
 * Dev-facing visual editor entrypoint.
 * <p>
 * Starts a local HTTP/WebSocket server on the client and opens the browser editor UI.
 */
public final class AuraTipEditorCommand {

    private AuraTipEditorCommand() {
    }

    public static void register() {
        CommandRegister.registerServer(AuraTipEditorCommand::registerCommand);
    }

    public static void registerCommand(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext context,
            Commands.CommandSelection environment
    ) {
        dispatcher.register(Commands.literal("auratip")
                .requires(source -> source.permissions().hasPermission(new Permission.HasCommandLevel(PermissionLevel.byId(2))))
                .then(Commands.literal("editor")
                        .executes(ctx -> {
                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                            new OpenEditorPacket("tip").sendTo(player);
                            ctx.getSource().sendSuccess(() -> Component.literal("AuraTip editor opened on client."), false);
                            return 1;
                        })
                )
        );
    }
}

