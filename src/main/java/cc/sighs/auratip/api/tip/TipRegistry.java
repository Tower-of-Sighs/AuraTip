package cc.sighs.auratip.api.tip;

import cc.sighs.auratip.data.TipData;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Runtime tips
 * <p>
 * Tips here are checked by the server trigger manager (in addition to datapack tips).
 */
public final class TipRegistry {

    private static final String OWNER_KUBEJS = "kubejs";

    private static final Map<String, List<TipData>> BY_OWNER = new LinkedHashMap<>();
    private static volatile List<TipData> SNAPSHOT = Collections.emptyList();

    private TipRegistry() {
    }

    /**
     * Returns the currently registered runtime tips.
     *
     * @return immutable list (never null)
     */
    public static List<TipData> getTips() {
        return SNAPSHOT;
    }

    /**
     * Returns runtime tips for a single owner.
     *
     * @param owner owner id (recommended: modid)
     * @return immutable list (never null)
     */
    public static synchronized List<TipData> getTips(@Nullable String owner) {
        String key = normalizeOwner(owner);
        List<TipData> list = BY_OWNER.get(key);
        return list == null ? List.of() : list;
    }

    /**
     * Replaces runtime tips for a single owner.
     *
     * @param owner   owner id (recommended: modid). {@code \"kubejs\"} is reserved for the KubeJS integration.
     * @param newTips tip collection; when null/empty, that owner becomes empty
     */
    public static synchronized void setTips(@Nullable String owner, @Nullable Collection<TipData> newTips) {
        String key = normalizeOwner(owner);
        if (newTips == null || newTips.isEmpty()) {
            BY_OWNER.remove(key);
        } else {
            // Validate within this owner: ids must be unique and non-null.
            LinkedHashMap<String, TipData> byId = new LinkedHashMap<>();
            for (TipData tip : newTips) {
                if (tip == null) {
                    throw new IllegalStateException("TipRegistry.setTips: tip is null (owner='" + key + "')");
                }
                if (tip.id() == null) {
                    throw new IllegalStateException("TipRegistry.setTips: tip.id is null (owner='" + key + "')");
                }
                String id = tip.id().toString();
                TipData previous = byId.put(id, tip);
                if (previous != null) {
                    throw new IllegalStateException("Duplicate TipData id '" + id + "' inside owner '" + key + "'");
                }
            }
            BY_OWNER.put(key, List.copyOf(byId.values()));
        }
        rebuildSnapshot();
    }

    /**
     * Clears runtime tips for a single owner.
     */
    public static synchronized void clear(@Nullable String owner) {
        String key = normalizeOwner(owner);
        BY_OWNER.remove(key);
        rebuildSnapshot();
    }

    public static synchronized void clearAll() {
        BY_OWNER.clear();
        rebuildSnapshot();
    }

    /**
     * Owner id reserved for the KubeJS integration.
     */
    public static String ownerKubejs() {
        return OWNER_KUBEJS;
    }

    private static String normalizeOwner(@Nullable String owner) {
        return (owner == null) ? "" : owner.trim();
    }

    private static void rebuildSnapshot() {
        if (BY_OWNER.isEmpty()) {
            SNAPSHOT = Collections.emptyList();
            return;
        }

        List<TipData> out = new ArrayList<>();
        LinkedHashMap<String, String> ownerByTipId = new LinkedHashMap<>();

        for (Map.Entry<String, List<TipData>> entry : BY_OWNER.entrySet()) {
            String owner = entry.getKey();
            List<TipData> list = entry.getValue();
            if (list == null || list.isEmpty()) {
                continue;
            }
            for (TipData tip : list) {
                if (tip == null || tip.id() == null) {
                    continue;
                }
                String id = tip.id().toString();
                String previousOwner = ownerByTipId.putIfAbsent(id, owner);
                if (previousOwner != null) {
                    throw new IllegalStateException("Duplicate TipData id '" + id + "' detected across owners. owner=" + previousOwner + " and owner=" + owner);
                }
                out.add(tip);
            }
        }

        SNAPSHOT = out.isEmpty() ? Collections.emptyList() : List.copyOf(out);
    }
}
