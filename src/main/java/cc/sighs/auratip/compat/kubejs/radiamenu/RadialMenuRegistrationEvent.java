package cc.sighs.auratip.compat.kubejs.radiamenu;

import cc.sighs.auratip.data.RadialMenuData;
import dev.latvian.mods.kubejs.event.EventJS;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RadialMenuRegistrationEvent extends EventJS {

    private final Map<String, RadialMenuBuilder> menus = new LinkedHashMap<>();

    public RadialMenuBuilder create(String id) {
        RadialMenuBuilder builder = new RadialMenuBuilder(id);
        menus.put(id, builder);
        return builder;
    }

    public void remove(String id) {
        menus.remove(id);
    }

    public List<RadialMenuData> buildAll() {
        List<RadialMenuData> result = new ArrayList<>();
        for (RadialMenuBuilder builder : menus.values()) {
            result.add(builder.build());
        }
        return result;
    }
}