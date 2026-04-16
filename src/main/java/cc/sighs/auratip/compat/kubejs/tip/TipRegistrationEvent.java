package cc.sighs.auratip.compat.kubejs.tip;

import cc.sighs.auratip.data.TipData;
import dev.latvian.mods.kubejs.event.KubeEvent;
import dev.latvian.mods.kubejs.typings.Info;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TipRegistrationEvent implements KubeEvent {

    private final Map<String, TipBuilder> tips = new LinkedHashMap<>();

    @Info("Create a new Tip and return its builder. The id can omit namespace (defaults to kubejs). Duplicate ids will throw.")
    public TipBuilder create(String id) {
        String key = normalizeId(id);
        if (tips.containsKey(key)) {
            throw new IllegalStateException("Duplicate tip id: " + key);
        }
        TipBuilder builder = new TipBuilder(key);
        tips.put(key, builder);
        return builder;
    }

    @Info("Remove a previously created Tip by id. The id can omit namespace (defaults to kubejs).")
    public void remove(String id) {
        tips.remove(normalizeId(id));
    }

    @Info("Internal: build all Tips created in this event into data objects.")
    public List<TipData> buildAll() {
        List<TipData> result = new ArrayList<>();
        for (TipBuilder builder : tips.values()) {
            result.add(builder.build());
        }
        return result;
    }

    private static String normalizeId(String id) {
        if (id == null || id.isEmpty()) {
            return "kubejs:tip";
        }
        if (id.indexOf(':') < 0) {
            return "kubejs:" + id;
        }
        return id;
    }
}
