package cc.sighs.auratip.compat.nekojs.radiamenu;

import cc.sighs.auratip.api.radiamenu.RadialMenuRegistry;
import cc.sighs.auratip.data.RadialMenuData;

import javax.annotation.Nullable;
import java.util.List;

public final class RadialMenuScriptRegistry {
    private RadialMenuScriptRegistry() {
    }

    public static List<RadialMenuData> getMenus() {
        return List.copyOf(RadialMenuRegistry.getMenus(RadialMenuRegistry.ownerKubejs()));
    }

    public static void setMenus(@Nullable List<RadialMenuData> menus) {
        RadialMenuRegistry.setMenus(RadialMenuRegistry.ownerKubejs(), menus);
    }
}
