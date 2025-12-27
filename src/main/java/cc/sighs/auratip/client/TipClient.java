package cc.sighs.auratip.client;

import cc.sighs.auratip.client.render.TipOverlay;
import cc.sighs.auratip.data.TipData;

import java.util.*;

public class TipClient {
    private static final Deque<QueuedTip> QUEUE = new ArrayDeque<>();

    public static void enqueueTips(List<TipData> tips, Map<String, String> variables) {
        if (tips == null || tips.isEmpty()) {
            return;
        }
        Map<String, String> vars = variables == null ? Map.of() : new HashMap<>(variables);
        for (TipData tip : tips) {
            QUEUE.addLast(new QueuedTip(tip, vars));
        }
        showNextIfIdle();
    }

    public static void onTipClosed() {
        showNextIfIdle();
    }

    public static void closeCurrentTip() {
        if (TipOverlay.INSTANCE.isActive()) {
            TipOverlay.INSTANCE.requestClose();
        }
    }

    static void showNextIfIdle() {
        if (TipOverlay.INSTANCE.isActive()) {
            return;
        }
        QueuedTip next = QUEUE.pollFirst();
        if (next != null) {
            TipOverlay.INSTANCE.show(next.tip, next.variables);
        }
    }

    private record QueuedTip(TipData tip, Map<String, String> variables) {
    }
}
