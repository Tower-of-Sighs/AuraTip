package cc.sighs.auratip.data.trigger;

import cc.sighs.auratip.compat.kubejs.tip.TipScriptRegistry;
import cc.sighs.auratip.compat.kubejs.tip.TipVariables;
import cc.sighs.auratip.data.TipData;
import cc.sighs.auratip.data.TipData.Trigger.Mode;
import cc.sighs.auratip.network.NetworkHandler;
import cc.sighs.auratip.network.ShowTipsPacket;
import com.mafuyu404.oelib.data.DataManagerBridge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class TipTriggerManager {
    private static final String SHOWN_TIPS_TAG = "auratip_shown_tips";

    private TipTriggerManager() {
    }

    public static void trigger(String type, ServerPlayer player) {
        var dataTips = DataManagerBridge.getDataList(TipData.class);
        var scriptTips = TipScriptRegistry.getTips();
        boolean hasDataTips = dataTips != null && !dataTips.isEmpty();
        boolean hasScriptTips = scriptTips != null && !scriptTips.isEmpty();
        if (!hasDataTips && !hasScriptTips) {
            return;
        }

        List<TipData> tips = new ArrayList<>();
        if (hasDataTips) {
            tips.addAll(dataTips);
        }
        if (hasScriptTips) {
            tips.addAll(scriptTips);
        }

        var normalizedType = type == null ? "" : type.toUpperCase(Locale.ROOT);

        var persistent = player.getPersistentData();
        var shown = persistent.getCompound(SHOWN_TIPS_TAG);

        List<TipData> toShow = new ArrayList<>();
        long now = player.level().getGameTime();

        for (TipData tip : tips) {
            var trigger = tip.trigger();
            if (trigger == null) {
                continue;
            }
            var triggerType = trigger.type();
            if (triggerType == null) {
                continue;
            }
            if (!normalizedType.equals(triggerType.toUpperCase(Locale.ROOT))) {
                continue;
            }

            var id = tip.id();
            var mode = trigger.mode();
            boolean once = mode == Mode.ONCE;

            if (once && shown.getBoolean(id)) {
                continue;
            }

            int cooldown = trigger.cooldown();
            if (!once && cooldown > 0) {
                long lastShown = shown.getLong(id + "_last");
                if (now - lastShown < cooldown) {
                    continue;
                }
            }

            toShow.add(tip);
            if (once) {
                shown.putBoolean(id, true);
            } else if (cooldown > 0) {
                shown.putLong(id + "_last", now);
            }
        }

        if (toShow.isEmpty()) {
            return;
        }

        persistent.put(SHOWN_TIPS_TAG, shown);

        var vars = TipVariables.snapshot();
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new ShowTipsPacket(toShow, vars));
    }
}
