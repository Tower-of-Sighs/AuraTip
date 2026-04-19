package cc.sighs.auratip.api.radiamenu;

import cc.sighs.auratip.data.RadialMenuData;
import cc.sighs.auratip.data.action.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Extra radial menu slots appended at runtime.
 */
public final class RadialMenuExtraSlots {

    private static final String OWNER_DEFAULT = "default";
    private static final String OWNER_KUBEJS = "kubejs";

    private static final Map<String, List<RadialMenuData.Slot>> BY_OWNER = new LinkedHashMap<>();
    private static final Map<String, Map<ResourceLocation, List<RadialMenuData.Slot>>> BY_OWNER_BY_MENU = new LinkedHashMap<>();
    private static volatile List<RadialMenuData.Slot> SNAPSHOT = Collections.emptyList();
    private static volatile Map<ResourceLocation, List<RadialMenuData.Slot>> SNAPSHOT_BY_MENU = Collections.emptyMap();

    private RadialMenuExtraSlots() {
    }

    /**
     * Adds an extra slot.
     * <p>
     * This is a global append: the slot will be merged into <b>any</b> base menu that gets opened.
     * If you want to append slots only for a specific base menu id, use {@link #addSlotForMenu(ResourceLocation, RadialMenuData.Slot)}.
     *
     * @param slot slot to add (ignored when null)
     */
    public static synchronized void addSlot(RadialMenuData.Slot slot) {
        addSlot(OWNER_DEFAULT, slot);
    }

    /**
     * Adds an extra slot for a specific owner.
     *
     * @param owner owner id (recommended: modid). {@code "kubejs"} is reserved for the KubeJS integration.
     * @param slot  slot to add (ignored when null)
     */
    public static synchronized void addSlot(String owner, RadialMenuData.Slot slot) {
        if (slot == null) {
            return;
        }
        String key = normalizeOwner(owner);
        List<RadialMenuData.Slot> list = BY_OWNER.computeIfAbsent(key, k -> new ArrayList<>());
        list.add(slot);
        rebuildSnapshot();
    }

    /**
     * Adds an extra slot that only applies to one base menu id.
     * <p>
     * This is the "scoped append" variant. The slot will only be merged when the base menu id matches {@code menuId}.
     *
     * @param menuId target base menu id
     * @param slot   slot to add (ignored when null)
     */
    public static synchronized void addSlotForMenu(ResourceLocation menuId, RadialMenuData.Slot slot) {
        addSlotForMenu(OWNER_DEFAULT, menuId, slot);
    }

    /**
     * Adds an extra slot that only applies to one base menu id for a specific owner.
     *
     * @param owner  owner id (recommended: modid). {@code "kubejs"} is reserved for the KubeJS integration.
     * @param menuId target base menu id
     * @param slot   slot to add (ignored when null)
     */
    public static synchronized void addSlotForMenu(String owner, ResourceLocation menuId, RadialMenuData.Slot slot) {
        if (menuId == null || slot == null) {
            return;
        }
        String key = normalizeOwner(owner);
        Map<ResourceLocation, List<RadialMenuData.Slot>> byMenu = BY_OWNER_BY_MENU.computeIfAbsent(key, k -> new LinkedHashMap<>());
        List<RadialMenuData.Slot> list = byMenu.computeIfAbsent(menuId, k -> new ArrayList<>());
        list.add(slot);
        rebuildSnapshot();
    }

    /**
     * Convenience helper to build and add an extra slot.
     *
     * @param name           slot name (must be non-empty)
     * @param icon           icon texture location
     * @param action         slot action (required)
     * @param text           optional slot label
     * @param highlightColor optional hover highlight color (argb hex)
     */
    public static void addSlot(
            String name,
            ResourceLocation icon,
            Action action,
            @Nullable Component text,
            @Nullable String highlightColor
    ) {
        if (name == null || name.isEmpty() || icon == null || action == null) {
            return;
        }
        addSlot(new RadialMenuData.Slot(
                name,
                icon,
                action,
                Optional.ofNullable(text),
                Optional.ofNullable(highlightColor)
        ));
    }

    /**
     * Convenience helper to build and add an extra slot for a specific base menu id.
     *
     * @param menuId         target base menu id
     * @param name           slot name (must be non-empty)
     * @param icon           icon texture location
     * @param action         slot action (required)
     * @param text           optional slot label
     * @param highlightColor optional hover highlight color (argb hex)
     */
    public static void addSlotForMenu(
            ResourceLocation menuId,
            String name,
            ResourceLocation icon,
            Action action,
            @Nullable Component text,
            @Nullable String highlightColor
    ) {
        if (menuId == null || name == null || name.isEmpty() || icon == null || action == null) {
            return;
        }
        addSlotForMenu(menuId, new RadialMenuData.Slot(
                name,
                icon,
                action,
                Optional.ofNullable(text),
                Optional.ofNullable(highlightColor)
        ));
    }

    /**
     * Removes an extra slot from the default owner.
     *
     * @param slot slot to remove (ignored when null)
     */
    public static synchronized void removeSlot(RadialMenuData.Slot slot) {
        removeSlot(OWNER_DEFAULT, slot);
    }

    /**
     * Removes an extra slot for a specific owner.
     *
     * @param owner owner id (recommended: modid)
     * @param slot  slot to remove (ignored when null)
     */
    public static synchronized void removeSlot(String owner, RadialMenuData.Slot slot) {
        if (slot == null) {
            return;
        }
        String key = normalizeOwner(owner);
        List<RadialMenuData.Slot> list = BY_OWNER.get(key);
        if (list != null && list.remove(slot)) {
            if (list.isEmpty()) {
                BY_OWNER.remove(key);
            }
            rebuildSnapshot();
        }
    }

    /**
     * Removes an extra slot from a specific base menu id (default owner).
     *
     * @param menuId target base menu id
     * @param slot   slot to remove (ignored when null)
     */
    public static synchronized void removeSlotForMenu(ResourceLocation menuId, RadialMenuData.Slot slot) {
        removeSlotForMenu(OWNER_DEFAULT, menuId, slot);
    }

    /**
     * Removes an extra slot from a specific base menu id for a given owner.
     *
     * @param owner  owner id (recommended: modid)
     * @param menuId target base menu id
     * @param slot   slot to remove (ignored when null)
     */
    public static synchronized void removeSlotForMenu(String owner, ResourceLocation menuId, RadialMenuData.Slot slot) {
        if (menuId == null || slot == null) {
            return;
        }
        String key = normalizeOwner(owner);
        Map<ResourceLocation, List<RadialMenuData.Slot>> byMenu = BY_OWNER_BY_MENU.get(key);
        if (byMenu == null) {
            return;
        }
        List<RadialMenuData.Slot> list = byMenu.get(menuId);
        if (list != null && list.remove(slot)) {
            if (list.isEmpty()) {
                byMenu.remove(menuId);
            }
            if (byMenu.isEmpty()) {
                BY_OWNER_BY_MENU.remove(key);
            }
            rebuildSnapshot();
        }
    }

    /**
     * Removes extra slots by name from the default owner.
     * <p>
     * This is a convenience helper. All slots with the same name will be removed.
     *
     * @param name slot name
     */
    public static synchronized void removeSlot(String name) {
        removeSlot(OWNER_DEFAULT, name);
    }

    /**
     * Removes extra slots by name for a specific owner.
     * <p>
     * This is a convenience helper. All slots with the same name will be removed.
     *
     * @param owner owner id (recommended: modid)
     * @param name  slot name
     */
    public static synchronized void removeSlot(String owner, String name) {
        if (name == null || name.isEmpty()) {
            return;
        }
        String key = normalizeOwner(owner);
        List<RadialMenuData.Slot> list = BY_OWNER.get(key);
        if (list == null || list.isEmpty()) {
            return;
        }
        boolean changed = list.removeIf(s -> name.equals(s.name()));
        if (changed) {
            if (list.isEmpty()) {
                BY_OWNER.remove(key);
            }
            rebuildSnapshot();
        }
    }

    /**
     * Removes extra slots by name from a specific base menu id (default owner).
     *
     * @param menuId target base menu id
     * @param name   slot name
     */
    public static synchronized void removeSlotForMenu(ResourceLocation menuId, String name) {
        removeSlotForMenu(OWNER_DEFAULT, menuId, name);
    }

    /**
     * Removes extra slots by name from a specific base menu id for a given owner.
     *
     * @param owner  owner id (recommended: modid)
     * @param menuId target base menu id
     * @param name   slot name
     */
    public static synchronized void removeSlotForMenu(String owner, ResourceLocation menuId, String name) {
        if (menuId == null || name == null || name.isEmpty()) {
            return;
        }
        String key = normalizeOwner(owner);
        Map<ResourceLocation, List<RadialMenuData.Slot>> byMenu = BY_OWNER_BY_MENU.get(key);
        if (byMenu == null) {
            return;
        }
        List<RadialMenuData.Slot> list = byMenu.get(menuId);
        if (list == null || list.isEmpty()) {
            return;
        }
        boolean changed = list.removeIf(s -> name.equals(s.name()));
        if (changed) {
            if (list.isEmpty()) {
                byMenu.remove(menuId);
            }
            if (byMenu.isEmpty()) {
                BY_OWNER_BY_MENU.remove(key);
            }
            rebuildSnapshot();
        }
    }

    /**
     * Returns global extra slots (slots that apply to all base menus).
     *
     * @return immutable list (never null)
     */
    public static synchronized List<RadialMenuData.Slot> getSlots() {
        return SNAPSHOT;
    }

    /**
     * Returns extra slots that should be merged into a specific base menu id.
     * <p>
     * This includes both:
     * <ul>
     *     <li>Global extra slots (added via {@link #addSlot(RadialMenuData.Slot)})</li>
     *     <li>Menu-scoped extra slots (added via {@link #addSlotForMenu(ResourceLocation, RadialMenuData.Slot)})</li>
     * </ul>
     *
     * @param menuId base menu id
     * @return immutable list (never null)
     */
    public static synchronized List<RadialMenuData.Slot> getSlotsForMenu(ResourceLocation menuId) {
        if (menuId == null) {
            return SNAPSHOT;
        }
        List<RadialMenuData.Slot> scoped = SNAPSHOT_BY_MENU.get(menuId);
        if (scoped == null || scoped.isEmpty()) {
            return SNAPSHOT;
        }
        if (SNAPSHOT.isEmpty()) {
            return scoped;
        }
        List<RadialMenuData.Slot> merged = new ArrayList<>(SNAPSHOT.size() + scoped.size());
        merged.addAll(SNAPSHOT);
        merged.addAll(scoped);
        return List.copyOf(merged);
    }

    /**
     * Returns extra slots for a single owner.
     */
    public static synchronized List<RadialMenuData.Slot> getSlots(String owner) {
        String key = normalizeOwner(owner);
        List<RadialMenuData.Slot> list = BY_OWNER.get(key);
        return list == null ? List.of() : List.copyOf(list);
    }

    /**
     * Clears all extra slots.
     */
    public static synchronized void clear() {
        clear(OWNER_DEFAULT);
    }

    /**
     * Clears extra slots for a single owner.
     */
    public static synchronized void clear(String owner) {
        String key = normalizeOwner(owner);
        BY_OWNER.remove(key);
        BY_OWNER_BY_MENU.remove(key);
        rebuildSnapshot();
    }

    /**
     * Clears all owners' extra slots.
     */
    public static synchronized void clearAll() {
        BY_OWNER.clear();
        BY_OWNER_BY_MENU.clear();
        rebuildSnapshot();
    }

    /**
     * Owner id reserved for the KubeJS integration.
     */
    public static String ownerKubejs() {
        return OWNER_KUBEJS;
    }

    private static String normalizeOwner(@Nullable String owner) {
        if (owner == null || owner.isBlank()) {
            return OWNER_DEFAULT;
        }
        return owner.trim();
    }

    private static void rebuildSnapshot() {
        if (BY_OWNER.isEmpty()) {
            SNAPSHOT = Collections.emptyList();
        } else {
            List<RadialMenuData.Slot> out = new ArrayList<>();
            for (List<RadialMenuData.Slot> list : BY_OWNER.values()) {
                if (list != null && !list.isEmpty()) {
                    out.addAll(list);
                }
            }
            SNAPSHOT = out.isEmpty() ? Collections.emptyList() : List.copyOf(out);
        }

        if (BY_OWNER_BY_MENU.isEmpty()) {
            SNAPSHOT_BY_MENU = Collections.emptyMap();
            return;
        }

        Map<ResourceLocation, List<RadialMenuData.Slot>> merged = new LinkedHashMap<>();
        for (Map<ResourceLocation, List<RadialMenuData.Slot>> byMenu : BY_OWNER_BY_MENU.values()) {
            if (byMenu == null || byMenu.isEmpty()) {
                continue;
            }
            for (Map.Entry<ResourceLocation, List<RadialMenuData.Slot>> entry : byMenu.entrySet()) {
                ResourceLocation menuId = entry.getKey();
                List<RadialMenuData.Slot> list = entry.getValue();
                if (menuId == null || list == null || list.isEmpty()) {
                    continue;
                }
                List<RadialMenuData.Slot> existing = merged.get(menuId);
                if (existing == null) {
                    merged.put(menuId, List.copyOf(list));
                } else {
                    List<RadialMenuData.Slot> combined = new ArrayList<>(existing.size() + list.size());
                    combined.addAll(existing);
                    combined.addAll(list);
                    merged.put(menuId, List.copyOf(combined));
                }
            }
        }
        SNAPSHOT_BY_MENU = merged.isEmpty() ? Collections.emptyMap() : Map.copyOf(merged);
    }
}
