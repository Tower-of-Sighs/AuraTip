package cc.sighs.auratip.compat.kubejs.radiamenu;

import cc.sighs.auratip.data.RadialMenuData;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

public class RadialMenuScriptRegistry {
    @Getter
    private static volatile List<RadialMenuData> menus = Collections.emptyList();

    public static void setMenus(List<RadialMenuData> newMenus) {
        if (newMenus == null || newMenus.isEmpty()) {
            menus = Collections.emptyList();
            return;
        }
        menus = List.copyOf(newMenus);
    }
}