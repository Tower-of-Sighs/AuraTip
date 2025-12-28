package cc.sighs.auratip.client;

import cc.sighs.auratip.client.render.RadialMenuOverlay;
import cc.sighs.auratip.compat.kubejs.radiamenu.slot.RadialMenuExtraSlotRegistry;
import cc.sighs.auratip.data.RadialMenuData;
import com.mafuyu404.oelib.data.DataManagerBridge;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class RadialMenuClient {
    public static void openMenu() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        if (RadialMenuOverlay.INSTANCE.isActive()) {
            RadialMenuOverlay.INSTANCE.close();
            return;
        }

        var dataMenus = DataManagerBridge.getDataList(RadialMenuData.class);
        if (dataMenus == null || dataMenus.isEmpty()) {
            return;
        }

        RadialMenuData base = dataMenus.get(0);
        var extraSlots = RadialMenuExtraSlotRegistry.getSlots();
        RadialMenuData effective = base;
        if (!extraSlots.isEmpty()) {
            List<RadialMenuData.Slot> merged = new ArrayList<>(base.slots());
            merged.addAll(extraSlots);
            effective = new RadialMenuData(base.menuSettings(), List.copyOf(merged));
        }

        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        RadialMenuOverlay.INSTANCE.open(effective, width, height, minecraft);
    }
}
