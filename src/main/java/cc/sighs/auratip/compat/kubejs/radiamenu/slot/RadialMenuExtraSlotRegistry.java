package cc.sighs.auratip.compat.kubejs.radiamenu.slot;

import cc.sighs.auratip.data.RadialMenuData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RadialMenuExtraSlotRegistry {

    private static final List<RadialMenuData.Slot> EXTRA_SLOTS = new ArrayList<>();

    public static synchronized void addSlot(RadialMenuData.Slot slot) {
        if (slot == null) {
            return;
        }
        EXTRA_SLOTS.add(slot);
    }

    public static synchronized List<RadialMenuData.Slot> getSlots() {
        if (EXTRA_SLOTS.isEmpty()) {
            return Collections.emptyList();
        }
        return List.copyOf(EXTRA_SLOTS);
    }

    public static synchronized void clear() {
        EXTRA_SLOTS.clear();
    }
}

