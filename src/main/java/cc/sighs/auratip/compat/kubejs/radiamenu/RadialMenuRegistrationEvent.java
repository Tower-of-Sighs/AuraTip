package cc.sighs.auratip.compat.kubejs.radiamenu;

import cc.sighs.auratip.data.RadialMenuData;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.typings.Info;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RadialMenuRegistrationEvent implements KubeEvent {

    private final Map<String, RadialMenuBuilder> menus = new LinkedHashMap<>();

    @Info("Create a new radial menu and return its builder. The id can omit namespace (defaults to kubejs). Duplicate ids will throw.")
    public RadialMenuBuilder create(String id) {
        String key = normalizeId(id);
        if (menus.containsKey(key)) {
            throw new IllegalStateException("Duplicate radial menu id: " + key);
        }
        RadialMenuBuilder builder = new RadialMenuBuilder(key);
        menus.put(key, builder);
        return builder;
    }

    @Info("Remove a previously created radial menu by id. The id can omit namespace (defaults to kubejs).")
    public void remove(String id) {
        menus.remove(normalizeId(id));
    }

    @Info("Internal: build all radial menus created in this event into data objects.")
    public List<RadialMenuData> buildAll() {
        List<RadialMenuData> result = new ArrayList<>();
        for (RadialMenuBuilder builder : menus.values()) {
            result.add(builder.build());
        }
        return result;
    }

    private static String normalizeId(String id) {
        if (id == null || id.isEmpty()) {
            return "kubejs:radial_menu";
        }
        if (id.indexOf(':') < 0) {
            return "kubejs:" + id;
        }
        return id;
    }
}
