package cc.sighs.auratip.dev;

import cc.sighs.auratip.api.tip.TipBuilder;
import cc.sighs.auratip.api.tip.TipRegistry;
import cc.sighs.auratip.api.tip.TipServer;
import cc.sighs.oelib.registry.extra.CommandRegister;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.Map;

public final class DevTestCommand {

    private DevTestCommand() {
    }

    public static void register() {
        CommandRegister.registerServer(DevTestCommand::registerCommand);
    }

    public static void registerCommand(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext context,
            Commands.CommandSelection environment
    ) {
        dispatcher.register(Commands.literal("auratipdev")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("tip")
                        .then(Commands.literal("trigger")
                                .then(Commands.argument("type", ResourceLocationArgument.id())
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            ResourceLocation type = ResourceLocationArgument.getId(ctx, "type");
                                            TipServer.trigger(type, player, buildVariables(player));
                                            ctx.getSource().sendSuccess(() -> Component.literal("TipServer.trigger: " + type), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("trigger_id")
                                .then(Commands.argument("id", ResourceLocationArgument.id())
                                        .executes(ctx -> {
                                            ServerPlayer player = ctx.getSource().getPlayerOrException();
                                            ResourceLocation id = ResourceLocationArgument.getId(ctx, "id");
                                            TipServer.triggerById(id, player, buildVariables(player));
                                            ctx.getSource().sendSuccess(() -> Component.literal("TipServer.triggerById: " + id), true);
                                            return 1;
                                        })
                                )
                        )
                        .then(Commands.literal("show")
                                .executes(ctx -> {
                                    ServerPlayer player = ctx.getSource().getPlayerOrException();

                                    ResourceLocation id = ResourceLocation.fromNamespaceAndPath("auratip", "dev_show_direct");
                                    var tip = new TipBuilder(id)
                                            .triggerRepeatable(ResourceLocation.fromNamespaceAndPath("auratip", "unused_trigger"), 0)
                                            .visual(v -> v
                                                    .animationStyle(ResourceLocation.fromNamespaceAndPath("auratip", "fade_and_slide"))
                                                    .hoverAnimationStyle(ResourceLocation.fromNamespaceAndPath("auratip", "none"))
                                                    .size(220, 62)
                                                    .positionPreset("BOTTOM_RIGHT")
                                            )
                                            .behavior(b -> b.duration(160))
                                            .page(0, p -> p
                                                    .title(Component.literal("TipServer.show 演示"), 0.8f, 0)
                                                    .content(Component.literal("这是直接 show 的 TipData，不走 trigger 匹配/冷却。\n玩家: ${player}"), 0.65f, 1)
                                            )
                                            .build();

                                    TipServer.show(player, tip, buildVariables(player));
                                    ctx.getSource().sendSuccess(() -> Component.literal("TipServer.show: " + tip.id()), true);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("runtime_count")
                                .executes(ctx -> {
                                    int count = TipRegistry.getTips().size();
                                    ctx.getSource().sendSuccess(() -> Component.literal("TipRegistry runtime tips: " + count), false);
                                    return 1;
                                })
                        )
                )
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
