package cc.sighs.auratip.client;

import cc.sighs.auratip.client.render.TipOverlay;
import cc.sighs.auratip.data.TipData;
import com.mafuyu404.oelib.data.DataManagerBridge;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class TipClient {
    private static final Deque<TipData> QUEUE = new ArrayDeque<>();

    public static void enqueueTipsById(List<String> ids) {
        List<TipData> tips = DataManagerBridge.getDataList(TipData.class);
        if (tips == null || tips.isEmpty()) {
            return;
        }
        for (String id : ids) {
            for (TipData tip : tips) {
                if (tip.id().equals(id)) {
                    QUEUE.addLast(tip);
                    break;
                }
            }
        }
        showNextIfIdle();
    }

    public static void onTipClosed() {
        showNextIfIdle();
    }

    public static void closeCurrentTip() {
        if (TipOverlay.INSTANCE.isActive()) {
            TipOverlay.INSTANCE.closeImmediately();
            onTipClosed();
        }
    }

    static void showNextIfIdle() {
        if (TipOverlay.INSTANCE.isActive()) {
            return;
        }
        TipData next = QUEUE.pollFirst();
        if (next != null) {
            TipOverlay.INSTANCE.show(next);
        }
    }
}
