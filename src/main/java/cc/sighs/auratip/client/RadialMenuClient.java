package cc.sighs.auratip.client;

import cc.sighs.auratip.client.render.RadialMenuOverlay;
import cc.sighs.auratip.data.RadialMenuData;
import com.mafuyu404.oelib.data.DataManagerBridge;
import net.minecraft.client.Minecraft;

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

        var menus = DataManagerBridge.getDataList(RadialMenuData.class);
        if (menus == null || menus.isEmpty()) {
            return;
        }

        var menu = menus.get(0);
        int width = minecraft.getWindow().getGuiScaledWidth();
        int height = minecraft.getWindow().getGuiScaledHeight();
        RadialMenuOverlay.INSTANCE.open(menu, width, height, minecraft);
    }
}
