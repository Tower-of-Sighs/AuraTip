package cc.sighs.auratip.compat.nekojs.tip;

import cc.sighs.auratip.api.client.TipClientApi;
import cc.sighs.auratip.api.tip.TipRegistry;
import cc.sighs.auratip.api.tip.TipServer;
import cc.sighs.auratip.data.TipData;
import cc.sighs.oelib.data.DataManager;
import com.tkisor.nekojs.NekoJS;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class TipTriggers {
    public static void trigger(String type, ServerPlayer player) {
        TipServer.trigger(normalizeType(type), player, TipVariables.snapshot());
    }

    public static void trigger(String type, ServerPlayer player, @Nullable Map<String, ?> variables) {
        TipServer.trigger(normalizeType(type), player, variables);
    }

    public static void triggerById(String tipId, ServerPlayer player) {
        TipServer.triggerById(normalizeTipId(tipId), player, TipVariables.snapshot());
    }

    public static void triggerById(String tipId, ServerPlayer player, @Nullable Map<String, ?> variables) {
        TipServer.triggerById(normalizeTipId(tipId), player, variables);
    }

    public static void show(ServerPlayer player, List<TipData> tips) {
        TipServer.show(player, tips, TipVariables.snapshot());
    }

    public static void show(ServerPlayer player, List<TipData> tips, @Nullable Map<String, ?> variables) {
        TipServer.show(player, tips, variables);
    }

    public static void show(ServerPlayer player, TipData tip) {
        TipServer.show(player, tip, TipVariables.snapshot());
    }

    public static void show(ServerPlayer player, TipData tip, @Nullable Map<String, ?> variables) {
        TipServer.show(player, tip, variables);
    }

    public static void showById(String tipId, ServerPlayer player) {
        TipData tip = findTipById(normalizeTipId(tipId));
        if (tip == null) {
            return;
        }
        TipServer.show(player, tip, TipVariables.snapshot());
    }

    public static void showById(String tipId, ServerPlayer player, @Nullable Map<String, ?> variables) {
        TipData tip = findTipById(normalizeTipId(tipId));
        if (tip == null) {
            return;
        }
        TipServer.show(player, tip, variables);
    }

    public static void enqueue(List<TipData> tips) {
        enqueue(tips, TipVariables.snapshot());
    }

    public static void enqueue(List<TipData> tips, @Nullable Map<String, ?> variables) {
        if (tips == null || tips.isEmpty()) {
            return;
        }
        TipClientApi.enqueue(tips, variables);
    }

    private static Identifier normalizeType(String type) {
        if (type == null || type.isEmpty()) {
            return Identifier.fromNamespaceAndPath(NekoJS.MODID, "trigger");
        }
        if (type.indexOf(':') < 0) {
            return Identifier.fromNamespaceAndPath(NekoJS.MODID, type);
        }
        return Identifier.parse(type);
    }

    private static Identifier normalizeTipId(String id) {
        if (id == null || id.isEmpty()) {
            return Identifier.fromNamespaceAndPath(NekoJS.MODID, "tip");
        }
        if (id.indexOf(':') < 0) {
            return Identifier.fromNamespaceAndPath(NekoJS.MODID, id);
        }
        return Identifier.parse(id);
    }

    @Nullable
    private static TipData findTipById(Identifier tipId) {
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
