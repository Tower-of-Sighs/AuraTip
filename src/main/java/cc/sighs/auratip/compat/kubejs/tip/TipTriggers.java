package cc.sighs.auratip.compat.kubejs.tip;

import cc.sighs.auratip.api.tip.TipServer;
import cc.sighs.auratip.api.tip.TipRegistry;
import cc.sighs.auratip.api.client.TipClientApi;
import cc.sighs.auratip.data.TipData;
import cc.sighs.oelib.data.DataManager;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class TipTriggers {
    @Info("Manually trigger a Tip trigger type for a player, using the current TipVariables snapshot. The type can omit namespace (defaults to kubejs).")
    public static void trigger(String type, ServerPlayer player) {
        TipServer.trigger(normalizeType(type), player, TipVariables.snapshot());
    }

    @Info("Manually trigger a Tip trigger type for a player, using an explicit variables map. The type can omit namespace (defaults to kubejs).")
    public static void trigger(String type, ServerPlayer player, @Nullable Map<String, ?> variables) {
        TipServer.trigger(normalizeType(type), player, variables);
    }

    @Info("Trigger a specific tip by its id (applies ONCE/cooldown rules). Uses the current TipVariables snapshot.")
    public static void triggerById(String tipId, ServerPlayer player) {
        TipServer.triggerById(normalizeTipId(tipId), player, TipVariables.snapshot());
    }

    @Info("Trigger a specific tip by its id (applies ONCE/cooldown rules), using an explicit variables map.")
    public static void triggerById(String tipId, ServerPlayer player, @Nullable Map<String, ?> variables) {
        TipServer.triggerById(normalizeTipId(tipId), player, variables);
    }

    @Info("Show tips immediately to a player (bypasses trigger filtering/cooldown), using the current TipVariables snapshot.")
    public static void show(ServerPlayer player, List<TipData> tips) {
        TipServer.show(player, tips, TipVariables.snapshot());
    }

    @Info("Show tips immediately to a player (bypasses trigger filtering/cooldown), using an explicit variables map.")
    public static void show(ServerPlayer player, List<TipData> tips, @Nullable Map<String, ?> variables) {
        TipServer.show(player, tips, variables);
    }

    @Info("Show a single tip immediately to a player, using the current TipVariables snapshot.")
    public static void show(ServerPlayer player, TipData tip) {
        TipServer.show(player, tip, TipVariables.snapshot());
    }

    @Info("Show a single tip immediately to a player, using an explicit variables map.")
    public static void show(ServerPlayer player, TipData tip, @Nullable Map<String, ?> variables) {
        TipServer.show(player, tip, variables);
    }

    @Info("Find a tip by id and show it immediately (bypasses trigger filtering/cooldown). Uses the current TipVariables snapshot.")
    public static void showById(String tipId, ServerPlayer player) {
        TipData tip = findTipById(normalizeTipId(tipId));
        if (tip == null) {
            return;
        }
        TipServer.show(player, tip, TipVariables.snapshot());
    }

    @Info("Find a tip by id and show it immediately (bypasses trigger filtering/cooldown), using an explicit variables map.")
    public static void showById(String tipId, ServerPlayer player, @Nullable Map<String, ?> variables) {
        TipData tip = findTipById(normalizeTipId(tipId));
        if (tip == null) {
            return;
        }
        TipServer.show(player, tip, variables);
    }

    @Info("Client-only: enqueue tips to be shown locally (bypasses all server-side trigger rules). Uses the current TipVariables snapshot.")
    public static void enqueue(List<TipData> tips) {
        enqueue(tips, TipVariables.snapshot());
    }

    @Info("Client-only: enqueue tips to be shown locally (bypasses all server-side trigger rules), using an explicit variables map.")
    public static void enqueue(List<TipData> tips, @Nullable Map<String, ?> variables) {
        if (tips == null || tips.isEmpty()) {
            return;
        }
        TipClientApi.enqueue(tips, variables);
    }

    private static ResourceLocation normalizeType(String type) {
        if (type == null || type.isEmpty()) {
            return new ResourceLocation("kubejs", "trigger");
        }
        if (type.indexOf(':') < 0) {
            return new ResourceLocation("kubejs", type);
        }
        return new ResourceLocation(type);
    }

    private static ResourceLocation normalizeTipId(String id) {
        if (id == null || id.isEmpty()) {
            return new ResourceLocation("kubejs", "tip");
        }
        if (id.indexOf(':') < 0) {
            return new ResourceLocation("kubejs", id);
        }
        return new ResourceLocation(id);
    }

    @Nullable
    private static TipData findTipById(ResourceLocation tipId) {
        if (tipId == null) {
            return null;
        }

        TipData found = null;

        var dataTips = DataManager.getDataList(TipData.class);
        if (dataTips != null && !dataTips.isEmpty()) {
            for (TipData tip : dataTips) {
                if (tip == null || tip.id() == null) {
                    continue;
                }
                if (tipId.equals(tip.id())) {
                    if (found != null) {
                        throw new IllegalStateException("Duplicate TipData id '" + tipId + "' detected in datapacks.");
                    }
                    found = tip;
                }
            }
        }

        var runtimeTips = TipRegistry.getTips();
        if (runtimeTips != null && !runtimeTips.isEmpty()) {
            for (TipData tip : runtimeTips) {
                if (tip == null || tip.id() == null) {
                    continue;
                }
                if (tipId.equals(tip.id())) {
                    if (found != null) {
                        throw new IllegalStateException("Duplicate TipData id '" + tipId + "' detected between datapacks and runtime tips.");
                    }
                    found = tip;
                }
            }
        }

        return found;
    }

}

