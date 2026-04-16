package cc.sighs.auratip.api.client;

import cc.sighs.auratip.client.TipClient;
import cc.sighs.auratip.data.TipData;
import cc.sighs.auratip.util.ResolveUtil;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

/**
 * Client-side helpers for showing tips.
 */
public final class TipClientApi {

    private TipClientApi() {
    }

    /**
     * Enqueues tips to be shown on the client.
     * <p>
     * This bypasses all server-side trigger rules (ONCE/cooldown, trigger type matching).
     *
     * @param tips      tips to show (order preserved)
     * @param variables variables for <code>${key}</code> placeholders (nullable). Values can be {@link Component}
     *                  or any other object (converted using {@code toString()}).
     */
    public static void enqueue(List<TipData> tips, @Nullable Map<String, ?> variables) {
        TipClient.enqueueTips(tips, ResolveUtil.toComponentMap(variables));
    }
}
