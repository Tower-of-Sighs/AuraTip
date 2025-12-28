package cc.sighs.auratip.compat.kubejs.radiamenu;

import cc.sighs.auratip.data.RadialMenuData;

import java.util.Collections;
import java.util.List;

public class RadialMenuScriptRegistry {
    private static volatile List<RadialMenuData> menus = Collections.emptyList();

    public static List<RadialMenuData> getMenus() {
        return menus;
    }

    public static void setMenus(List<RadialMenuData> newMenus) {
        if (newMenus == null || newMenus.isEmpty()) {
            menus = Collections.emptyList();
            return;
        }
        menus = List.copyOf(newMenus);
    }
}