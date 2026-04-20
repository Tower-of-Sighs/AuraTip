package cc.sighs.auratip.client;

import cc.sighs.auratip.api.radiamenu.RadialMenuExtraSlots;
import cc.sighs.auratip.api.radiamenu.RadialMenuRegistry;
import cc.sighs.auratip.client.render.RadialMenuOverlay;
import cc.sighs.auratip.data.RadialMenuData;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RadialMenuClient {

    public static void openMenu(Identifier menuId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;

        if (RadialMenuOverlay.INSTANCE.isActive()) {
            RadialMenuOverlay.INSTANCE.close();
            return;
        }

        RadialMenuData menuData = buildMenuData(menuId);
        if (menuData != null) {
            openMenuAtCenter(minecraft, menuData);
        }
    }

    private static RadialMenuData buildMenuData(Identifier menuId) {
        RadialMenuData baseMenu = getBaseMenu(menuId);
        if (baseMenu == null) return null;

        return enhanceWithExtraSlots(baseMenu);
    }

    private static RadialMenuData getBaseMenu(Identifier menuId) {
        return RadialMenuRegistry.resolveMenuToOpen(menuId);
    }

    private static RadialMenuData enhanceWithExtraSlots(RadialMenuData baseMenu) {
        List<RadialMenuData.Slot> extraSlots = RadialMenuExtraSlots.getSlotsForMenu(baseMenu.id());
        if (extraSlots.isEmpty()) return baseMenu;

        List<RadialMenuData.Slot> combinedSlots = Stream.concat(
                        baseMenu.slots().stream(),
                        extraSlots.stream()
                )
                .collect(Collectors.toMap(
                        RadialMenuData.Slot::name,
                        slot -> slot,
                        (_, replacement) -> replacement,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();

        return new RadialMenuData(baseMenu.id(), baseMenu.menuSettings(), combinedSlots);
    }

    private static void openMenuAtCenter(Minecraft minecraft, RadialMenuData menuData) {
        var window = minecraft.getWindow();
        RadialMenuOverlay.INSTANCE.open(
                menuData,
                window.getGuiScaledWidth(),
                window.getGuiScaledHeight(),
                minecraft
        );
    }
}
