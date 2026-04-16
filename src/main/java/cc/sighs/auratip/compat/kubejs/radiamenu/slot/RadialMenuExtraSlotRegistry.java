package cc.sighs.auratip.compat.kubejs.radiamenu.slot;

import cc.sighs.auratip.api.radiamenu.RadialMenuExtraSlots;
import cc.sighs.auratip.data.RadialMenuData;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class RadialMenuExtraSlotRegistry {

    public static synchronized void addSlot(RadialMenuData.Slot slot) {
        RadialMenuExtraSlots.addSlot(RadialMenuExtraSlots.ownerKubejs(), slot);
    }

    public static synchronized void addSlotForMenu(ResourceLocation menuId, RadialMenuData.Slot slot) {
        RadialMenuExtraSlots.addSlotForMenu(RadialMenuExtraSlots.ownerKubejs(), menuId, slot);
    }

    public static synchronized List<RadialMenuData.Slot> getSlots() {
        return RadialMenuExtraSlots.getSlots();
    }

    public static synchronized void removeSlot(String name) {
        RadialMenuExtraSlots.removeSlot(RadialMenuExtraSlots.ownerKubejs(), name);
    }

    public static synchronized void removeSlotForMenu(ResourceLocation menuId, String name) {
        RadialMenuExtraSlots.removeSlotForMenu(RadialMenuExtraSlots.ownerKubejs(), menuId, name);
    }

    public static synchronized void clear() {
        RadialMenuExtraSlots.clear(RadialMenuExtraSlots.ownerKubejs());
    }
}

