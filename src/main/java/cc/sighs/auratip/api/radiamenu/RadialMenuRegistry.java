package cc.sighs.auratip.api.radiamenu;

import cc.sighs.auratip.data.RadialMenuData;
import cc.sighs.oelib.data.DataManager;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Runtime radial menus.
 * <p>
 * You can register multiple base menus (one per id). A menu can come from:
 * <ul>
 *     <li>Datapacks: {@code data/auratip/radial_menu/*.json} (AuraTip currently only loads its own namespace)</li>
 *     <li>Runtime: {@link #setMenus(String, Collection)} (Java/KubeJS)</li>
 * </ul>
 * <p>
 * Rendering note: AuraTip's overlay is a singleton. If you try to open a menu while another is already open,
 * the overlay code will close the current one first (no exception is thrown, but you may see a "toggle" behavior).
 * <p>
 * Id rule: {@link RadialMenuData#id()} must be globally unique across all sources.
 * Duplicates throw immediately.
 */
public final class RadialMenuRegistry {

    private static final String OWNER_DEFAULT = "default";
    private static final String OWNER_KUBEJS = "kubejs";

    private static final Map<String, Map<ResourceLocation, RadialMenuData>> BY_OWNER = new LinkedHashMap<>();
    private static volatile Map<ResourceLocation, RadialMenuData> SNAPSHOT_BY_ID = Collections.emptyMap();

    private RadialMenuRegistry() {
    }

    /**
     * Owner id reserved for the KubeJS integration.
     */
    public static String ownerKubejs() {
        return OWNER_KUBEJS;
    }

    /**
     * Returns all runtime menus across all owners.
     */
    public static Collection<RadialMenuData> getAllRuntimeMenus() {
        return SNAPSHOT_BY_ID.values();
    }

    /**
     * Returns runtime menus for a single owner.
     */
    public static synchronized Collection<RadialMenuData> getMenus(@Nullable String owner) {
        String key = normalizeOwner(owner);
        Map<ResourceLocation, RadialMenuData> map = BY_OWNER.get(key);
        if (map == null || map.isEmpty()) {
            return List.of();
        }
        return map.values();
    }

    /**
     * Returns a runtime menu by id.
     */
    public static @Nullable RadialMenuData getRuntimeMenu(ResourceLocation id) {
        if (id == null) {
            return null;
        }
        return SNAPSHOT_BY_ID.get(id);
    }

    /**
     * Returns a datapack menu by id (if present).
     * <p>
     * Duplicated datapack ids throw.
     */
    public static @Nullable RadialMenuData getDataMenu(ResourceLocation id) {
        if (id == null) {
            return null;
        }
        return getDataMenusById().get(id);
    }

    /**
     * Replaces all runtime menus for an owner.
     * <p>
     * Duplicate ids are not allowed:
     * <ul>
     *     <li>Within the same owner</li>
     *     <li>Across owners</li>
     *     <li>Between runtime and datapack menus</li>
     * </ul>
     */
    public static synchronized void setMenus(@Nullable String owner, @Nullable Collection<RadialMenuData> menus) {
        String key = normalizeOwner(owner);
        if (menus == null || menus.isEmpty()) {
            BY_OWNER.remove(key);
            rebuildSnapshot();
            return;
        }

        Map<ResourceLocation, RadialMenuData> map = new LinkedHashMap<>();
        for (RadialMenuData menu : menus) {
            if (menu == null) {
                throw new IllegalStateException("RadialMenuRegistry.setMenus: menu is null (owner='" + key + "')");
            }
            ResourceLocation id = Objects.requireNonNull(menu.id(), "menu.id()");
            if (map.containsKey(id)) {
                throw new IllegalStateException("Duplicate radial menu id '" + id + "' within owner '" + key + "'");
            }
            map.put(id, menu);
        }

        // Ensure no conflict with datapack ids.
        Map<ResourceLocation, RadialMenuData> data = getDataMenusById();
        for (ResourceLocation id : map.keySet()) {
            if (data.containsKey(id)) {
                throw new IllegalStateException("Duplicate radial menu id '" + id + "' detected between runtime and datapack menus.");
            }
        }

        BY_OWNER.put(key, Map.copyOf(map));
        rebuildSnapshot();
    }

    /**
     * Clears runtime menus for a single owner.
     */
    public static synchronized void clear(@Nullable String owner) {
        setMenus(owner, null);
    }

    /**
     * Clears all runtime menus.
     */
    public static synchronized void clearAll() {
        BY_OWNER.clear();
        rebuildSnapshot();
    }

    /**
     * Resolves a menu to open.
     * <p>
     * - If {@code menuId} is provided, this searches runtime first, then datapack.
     * - If {@code menuId} is null, this only returns a menu when exactly one menu exists across all sources.
     * (If multiple menus exist, the caller must provide an id.)
     */
    public static @Nullable RadialMenuData resolveMenuToOpen(@Nullable ResourceLocation menuId) {
        if (menuId != null) {
            RadialMenuData runtime = getRuntimeMenu(menuId);
            if (runtime != null) {
                return runtime;
            }
            return getDataMenu(menuId);
        }

        Map<ResourceLocation, RadialMenuData> data = getDataMenusById();
        int runtimeCount = SNAPSHOT_BY_ID.size();
        int dataCount = data.size();
        int total = runtimeCount + dataCount;

        if (total == 0) {
            return null;
        }
        if (total == 1) {
            if (runtimeCount == 1) {
                return SNAPSHOT_BY_ID.values().iterator().next();
            }
            return data.values().iterator().next();
        }

        // Ambiguous: multiple menus exist, caller should specify id.
        return null;
    }

    private static String normalizeOwner(@Nullable String owner) {
        if (owner == null || owner.isBlank()) {
            return OWNER_DEFAULT;
        }
        return owner.trim();
    }

    private static void rebuildSnapshot() {
        if (BY_OWNER.isEmpty()) {
            SNAPSHOT_BY_ID = Collections.emptyMap();
            return;
        }

        Map<ResourceLocation, RadialMenuData> merged = new LinkedHashMap<>();
        for (Map.Entry<String, Map<ResourceLocation, RadialMenuData>> ownerEntry : BY_OWNER.entrySet()) {
            String owner = ownerEntry.getKey();
            Map<ResourceLocation, RadialMenuData> map = ownerEntry.getValue();
            if (map == null || map.isEmpty()) {
                continue;
            }
            for (Map.Entry<ResourceLocation, RadialMenuData> entry : map.entrySet()) {
                ResourceLocation id = entry.getKey();
                if (merged.containsKey(id)) {
                    throw new IllegalStateException("Duplicate radial menu id '" + id + "' detected across runtime owners (at least '" + owner + "').");
                }
                merged.put(id, entry.getValue());
            }
        }
        SNAPSHOT_BY_ID = merged.isEmpty() ? Collections.emptyMap() : Map.copyOf(merged);
    }

    private static Map<ResourceLocation, RadialMenuData> getDataMenusById() {
        List<RadialMenuData> data = DataManager.getDataList(RadialMenuData.class);
        if (data == null || data.isEmpty()) {
            return Map.of();
        }
        Map<ResourceLocation, RadialMenuData> out = new LinkedHashMap<>();
        for (RadialMenuData menu : data) {
            if (menu == null) {
                continue;
            }
            ResourceLocation id = Objects.requireNonNull(menu.id(), "datapack radial menu id");
            if (out.containsKey(id)) {
                throw new IllegalStateException("Duplicate datapack radial menu id '" + id + "' detected.");
            }
            out.put(id, menu);
        }
        return out;
    }
}
