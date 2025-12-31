package cc.sighs.auratip.compat.kubejs.tip;

import cc.sighs.auratip.data.TipData;

import java.util.Collections;
import java.util.List;

public final class TipScriptRegistry {
    private static volatile List<TipData> tips = Collections.emptyList();

    private TipScriptRegistry() {
    }

    public static List<TipData> getTips() {
        return tips;
    }

    public static void setTips(List<TipData> newTips) {
        if (newTips == null || newTips.isEmpty()) {
            tips = Collections.emptyList();
            return;
        }
        tips = List.copyOf(newTips);
    }
}

