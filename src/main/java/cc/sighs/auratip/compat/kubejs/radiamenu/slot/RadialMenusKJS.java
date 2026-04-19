package cc.sighs.auratip.compat.kubejs.radiamenu.slot;

import cc.sighs.auratip.api.client.RadialMenuClientApi;
import cc.sighs.auratip.data.RadialMenuData;
import cc.sighs.auratip.data.action.Action;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;

public class RadialMenusKJS {
    @Info("Client-only: open a radial menu by id. The id can omit namespace (defaults to kubejs).")
    public static void open(String menuId) {
        if (menuId == null || menuId.isEmpty()) {
            return;
        }
        RadialMenuClientApi.open(normalizeId(menuId));
    }


    /**
     * Appends a slot to <b>all</b> base menus.
     * <p>
     * AuraTip's extra slots are merged at open-time. If you append a slot without specifying a base menu id,
     * it will affect every menu (datapack / Java runtime / KubeJS runtime) that gets opened.
     * <p>
     * If you only want to append to a specific base menu id, use {@link #addSlot(String, String, String, Action, Component, String)}.
     */
    @Info("Append an extra slot to all base radial menus. The slot is merged into the final menu at open-time.")
    public static void addSlot(String name, String iconId, Action action, Component text, String highlightColor) {
        if (name == null || name.isEmpty()) {
            return;
        }
        if (iconId == null || iconId.isEmpty()) {
            return;
        }
        if (action == null) {
            return;
        }
        ResourceLocation icon = ResourceLocation.parse(iconId);
        RadialMenuData.Slot slot = new RadialMenuData.Slot(
                name,
                icon,
                action,
                Optional.ofNullable(text),
                Optional.ofNullable(highlightColor)
        );
        RadialMenuExtraSlotRegistry.addSlot(slot);
    }

    @Info("Remove extra slots by name from all base radial menus. Removes all slots with the same name added via KubeJS.")
    public static void removeSlot(String name) {
        if (name == null || name.isEmpty()) {
            return;
        }
        RadialMenuExtraSlotRegistry.removeSlot(name);
    }

    /**
     * Appends a slot to a specific base menu id.
     *
     * @param menuId         base menu id (ResourceLocation string)
     * @param name           slot name
     * @param iconId         icon texture location (ResourceLocation string)
     * @param action         slot action
     * @param text           optional label
     * @param highlightColor optional highlight color (argb hex)
     */
    @Info("Append an extra slot to a specific base menu id (only affects that menu). menuId is a ResourceLocation string.")
    public static void addSlot(String menuId, String name, String iconId, Action action, Component text, String highlightColor) {
        if (menuId == null || menuId.isEmpty()) {
            return;
        }
        if (name == null || name.isEmpty()) {
            return;
        }
        if (iconId == null || iconId.isEmpty()) {
            return;
        }
        if (action == null) {
            return;
        }

        ResourceLocation targetMenuId = ResourceLocation.parse(menuId);
        ResourceLocation icon = ResourceLocation.parse(iconId);
        RadialMenuData.Slot slot = new RadialMenuData.Slot(
                name,
                icon,
                action,
                Optional.ofNullable(text),
                Optional.ofNullable(highlightColor)
        );
        RadialMenuExtraSlotRegistry.addSlotForMenu(targetMenuId, slot);
    }

    @Info("Remove extra slots by name from a specific base menu id. Removes all slots with the same name added via KubeJS for that menu.")
    public static void removeSlotForMenu(String menuId, String name) {
        if (menuId == null || menuId.isEmpty()) {
            return;
        }
        if (name == null || name.isEmpty()) {
            return;
        }
        RadialMenuExtraSlotRegistry.removeSlotForMenu(normalizeId(menuId), name);
    }

    private static ResourceLocation normalizeId(String id) {
        if (id == null || id.isEmpty()) {
            return ResourceLocation.fromNamespaceAndPath("kubejs", "radial_menu");
        }
        if (id.indexOf(':') < 0) {
            return ResourceLocation.fromNamespaceAndPath("kubejs", id);
        }
        return ResourceLocation.parse(id);
    }

}
