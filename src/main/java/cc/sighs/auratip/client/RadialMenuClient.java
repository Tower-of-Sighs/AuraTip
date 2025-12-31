package cc.sighs.auratip.client;

import cc.sighs.auratip.client.render.RadialMenuOverlay;
import cc.sighs.auratip.compat.kubejs.radiamenu.RadialMenuScriptRegistry;
import cc.sighs.auratip.compat.kubejs.radiamenu.slot.RadialMenuExtraSlotRegistry;
import cc.sighs.auratip.data.RadialMenuData;
import com.mafuyu404.oelib.data.DataManagerBridge;
import net.minecraft.client.Minecraft;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RadialMenuClient {
    public static void openMenu() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;

        if (RadialMenuOverlay.INSTANCE.isActive()) {
            RadialMenuOverlay.INSTANCE.close();
            return;
        }

        RadialMenuData menuData = buildMenuData();
        if (menuData != null) {
            openMenuAtCenter(minecraft, menuData);
        }
    }

    private static RadialMenuData buildMenuData() {
        RadialMenuData baseMenu = getFirstAvailableMenu();
        if (baseMenu == null) return null;

        return enhanceWithExtraSlots(baseMenu);
    }

    private static RadialMenuData getFirstAvailableMenu() {
        return Optional.ofNullable(RadialMenuScriptRegistry.getMenus())
                .filter(list -> !list.isEmpty())
                .map(list -> list.get(0))
                .orElseGet(() -> Optional.ofNullable(DataManagerBridge.getDataList(RadialMenuData.class))
                        .filter(list -> !list.isEmpty())
                        .map(list -> list.get(0))
                        .orElse(null));
    }

    private static RadialMenuData enhanceWithExtraSlots(RadialMenuData baseMenu) {
        List<RadialMenuData.Slot> extraSlots = RadialMenuExtraSlotRegistry.getSlots();
        if (extraSlots.isEmpty()) return baseMenu;

        List<RadialMenuData.Slot> combinedSlots = Stream.concat(
                        baseMenu.slots().stream(),
                        extraSlots.stream()
                )
                .collect(Collectors.toMap(
                        RadialMenuData.Slot::name,
                        slot -> slot,
                        (existing, replacement) -> replacement,
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();

        return new RadialMenuData(baseMenu.menuSettings(), combinedSlots);
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
