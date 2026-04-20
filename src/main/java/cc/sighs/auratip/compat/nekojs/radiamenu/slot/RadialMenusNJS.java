package cc.sighs.auratip.compat.nekojs.radiamenu.slot;

import cc.sighs.auratip.api.client.RadialMenuClientApi;
import cc.sighs.auratip.data.RadialMenuData;
import cc.sighs.auratip.data.action.Action;
import com.tkisor.nekojs.NekoJS;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Optional;

public class RadialMenusNJS {
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
        Identifier icon = Identifier.parse(iconId);
        RadialMenuData.Slot slot = new RadialMenuData.Slot(
                name,
                icon,
                action,
                Optional.ofNullable(text),
                Optional.ofNullable(highlightColor)
        );
        RadialMenuExtraSlotRegistry.addSlot(slot);
    }

    public static void removeSlot(String name) {
        if (name == null || name.isEmpty()) {
            return;
        }
        RadialMenuExtraSlotRegistry.removeSlot(name);
    }

    /**
     * Appends a slot to a specific base menu id.
     *
     * @param menuId         base menu id (Identifier string)
     * @param name           slot name
     * @param iconId         icon texture location (Identifier string)
     * @param action         slot action
     * @param text           optional label
     * @param highlightColor optional highlight color (argb hex)
     */
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

        Identifier targetMenuId = Identifier.parse(menuId);
        Identifier icon = Identifier.parse(iconId);
        RadialMenuData.Slot slot = new RadialMenuData.Slot(
                name,
                icon,
                action,
                Optional.ofNullable(text),
                Optional.ofNullable(highlightColor)
        );
        RadialMenuExtraSlotRegistry.addSlotForMenu(targetMenuId, slot);
    }

    public static void removeSlotForMenu(String menuId, String name) {
        if (menuId == null || menuId.isEmpty()) {
            return;
        }
        if (name == null || name.isEmpty()) {
            return;
        }
        RadialMenuExtraSlotRegistry.removeSlotForMenu(normalizeId(menuId), name);
    }

    private static Identifier normalizeId(String id) {
        if (id == null || id.isEmpty()) {
            return Identifier.fromNamespaceAndPath(NekoJS.MODID, "radial_menu");
        }
        if (id.indexOf(':') < 0) {
            return Identifier.fromNamespaceAndPath(NekoJS.MODID, id);
        }
        return Identifier.parse(id);
    }

}
