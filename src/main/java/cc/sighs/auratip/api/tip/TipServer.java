package cc.sighs.auratip.api.tip;

import cc.sighs.auratip.data.TipData;
import cc.sighs.auratip.data.trigger.TipTriggerManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Server-side tip helpers.
 * <p>
 * Use {@link #trigger(Identifier, ServerPlayer, Map)} to trigger tips by type, or {@link #show(ServerPlayer, List, Map)}
 * to show tips immediately.
 * <p>
 * Variables: tip text supports <code>${key}</code> placeholders; pass variables as a map (values can be
 * {@link Component} or any object).
 */
public final class TipServer {

    private TipServer() {
    }

    /**
     * Triggers all tips matching {@code type} for a given player, using no variables.
     * <p>
     * This checks both datapack tips (data-driven) and runtime tips ({@link TipRegistry}).
     *
     * @param type   trigger type id
     * @param player target player
     */
    public static void trigger(Identifier type, ServerPlayer player) {
        TipTriggerManager.trigger(type, player, Map.of());
    }

    /**
     * Triggers tips by {@code TipData.trigger.type}.
     * <p>
     * This checks both datapack tips (data-driven) and runtime tips ({@link TipRegistry}).
     *
     * @param type      trigger type id
     * @param player    target player
     * @param variables variables used for <code>${key}</code> placeholders (nullable)
     */
    public static void trigger(Identifier type, ServerPlayer player, @Nullable Map<String, ?> variables) {
        TipTriggerManager.trigger(type, player, variables);
    }

    /**
     * Triggers tips by {@link cc.sighs.auratip.data.TipData#id()} (applies ONCE / cooldown rules).
     */
    public static void triggerById(Identifier tipId, ServerPlayer player, @Nullable Map<String, ?> variables) {
        TipTriggerManager.triggerById(tipId, player, variables);
    }

    /**
     * Sends tips directly to the player without performing trigger filtering / cooldown logic.
     * <p>
     * This is useful for "show now" behavior (for example, a modded GUI tutorial).
     *
     * @param player    target player
     * @param tips      tips to show (order preserved)
     * @param variables variables for <code>${key}</code> placeholders (nullable)
     */
    public static void show(ServerPlayer player, List<TipData> tips, @Nullable Map<String, ?> variables) {
        TipTriggerManager.showDirect(player, tips, variables);
    }

    /**
     * Convenience overload for showing a single tip.
     */
    public static void show(ServerPlayer player, TipData tip, @Nullable Map<String, ?> variables) {
        if (tip == null) {
            return;
        }
        show(player, List.of(tip), variables);
    }
}
