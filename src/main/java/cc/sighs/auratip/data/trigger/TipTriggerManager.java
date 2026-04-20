package cc.sighs.auratip.data.trigger;

import cc.sighs.auratip.api.tip.TipRegistry;
import cc.sighs.auratip.data.TipData;
import cc.sighs.auratip.data.TipData.Trigger.Mode;
import cc.sighs.auratip.network.ShowTipsPacket;
import cc.sighs.auratip.util.ResolveUtil;
import cc.sighs.oelib.data.DataManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public final class TipTriggerManager {
    private static final String SHOWN_TIPS_TAG = "auratip_shown_tips";

    private TipTriggerManager() {
    }

    /**
     * Triggers all tips whose trigger type matches {@code type}.
     * <p>
     * This checks both datapack tips (data-driven) and runtime tips ({@link TipRegistry}).
     *
     * @param type      trigger type id
     * @param player    player to send tips to
     * @param variables variables for <code>${key}</code> placeholders (nullable). Values can be {@link Component}
     *                  or any other object (converted using {@code toString()}).
     */
    public static void trigger(Identifier type, ServerPlayer player, Map<String, ?> variables) {
        if (type == null) return;

        List<TipData> toShow = collectTipsToShow(player, tip -> {
            var trigger = tip.trigger();
            if (trigger == null) return false;
            var triggerType = trigger.type();
            return type.equals(triggerType);
        });

        if (toShow.isEmpty()) return;

        var payloadTips = toShow.stream().map(ShowTipsPacket.TipEntry::new).toList();
        new ShowTipsPacket(payloadTips, ResolveUtil.toComponentMap(variables)).sendTo(player);
    }

    /**
     * Triggers tips by {@link TipData#id()}.
     * <p>
     * This applies the same ONCE / REPEATABLE / cooldown rules as {@link #trigger(Identifier, ServerPlayer, Map)}.
     *
     * @param tipId     tip id (recommended to be namespaced like {@code modid:my_tip})
     * @param player    player to send tips to
     * @param variables variables for <code>${key}</code> placeholders (nullable)
     */
    public static void triggerById(Identifier tipId, ServerPlayer player, Map<String, ?> variables) {
        if (tipId == null) return;

        List<TipData> toShow = collectTipsToShow(player, tip ->
                tip != null && tipId.equals(tip.id())
        );

        if (toShow.isEmpty()) return;

        var payloadTips = toShow.stream().map(ShowTipsPacket.TipEntry::new).toList();
        new ShowTipsPacket(payloadTips, ResolveUtil.toComponentMap(variables)).sendTo(player);
    }

    private static List<TipData> collectTipsToShow(
            ServerPlayer player,
            Predicate<TipData> filter
    ) {
        var dataTips = DataManager.getDataList(TipData.class);
        var runtimeTips = TipRegistry.getTips();
        boolean hasDataTips = dataTips != null && !dataTips.isEmpty();
        boolean hasRuntimeTips = runtimeTips != null && !runtimeTips.isEmpty();
        if (!hasDataTips && !hasRuntimeTips) {
            return List.of();
        }

        LinkedHashMap<Identifier, TipData> byId = new LinkedHashMap<>();
        if (hasDataTips) {
            for (TipData tip : dataTips) {
                if (tip == null || tip.id() == null) {
                    continue;
                }
                TipData previous = byId.putIfAbsent(tip.id(), tip);
                if (previous != null) {
                    throw new IllegalStateException("Duplicate TipData id '" + tip.id() + "' detected in datapacks. Tip ids must be globally unique.");
                }
            }
        }
        if (hasRuntimeTips) {
            for (TipData tip : runtimeTips) {
                if (tip == null || tip.id() == null) {
                    continue;
                }
                TipData previous = byId.putIfAbsent(tip.id(), tip);
                if (previous != null) {
                    throw new IllegalStateException("Duplicate TipData id '" + tip.id() + "' detected between datapacks and runtime tips. Tip ids must be globally unique.");
                }
            }
        }

        List<TipData> tips = new ArrayList<>(byId.values());

        var persistent = player.getPersistentData();
        var shown = persistent.getCompoundOrEmpty(SHOWN_TIPS_TAG);
        long now = player.level().getGameTime();

        List<TipData> toShow = new ArrayList<>();
        boolean dirty = false;

        for (TipData tip : tips) {
            if (!filter.test(tip)) {
                continue;
            }

            var trigger = tip.trigger();
            if (trigger == null) {
                continue;
            }

            String id = tip.id().toString();
            var mode = trigger.mode();
            boolean once = mode == Mode.ONCE;

            if (once && shown.getBooleanOr(id, false)) {
                continue;
            }

            int cooldown = trigger.cooldown();
            if (!once && cooldown > 0) {
                long lastShown = shown.getLongOr(id + "_last", 0L);
                if (now - lastShown < cooldown) {
                    continue;
                }
            }

            toShow.add(tip);
            dirty = true;
            if (once) {
                shown.putBoolean(id, true);
            } else if (cooldown > 0) {
                shown.putLong(id + "_last", now);
            }
        }

        if (dirty) {
            persistent.put(SHOWN_TIPS_TAG, shown);
        }

        return toShow;
    }

    /**
     * Sends tips directly to the player without trigger/cooldown filtering.
     *
     * @param player    target player
     * @param tips      tips to send (nullable/empty is ignored)
     * @param variables variables for <code>${key}</code> placeholders (nullable)
     */
    public static void showDirect(ServerPlayer player, List<TipData> tips, Map<String, ?> variables) {
        if (player == null || tips == null || tips.isEmpty()) {
            return;
        }
        var payloadTips = tips.stream().map(ShowTipsPacket.TipEntry::new).toList();
        new ShowTipsPacket(payloadTips, ResolveUtil.toComponentMap(variables)).sendTo(player);
    }
}
