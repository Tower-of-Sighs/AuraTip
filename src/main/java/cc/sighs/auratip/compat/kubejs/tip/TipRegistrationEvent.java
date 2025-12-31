package cc.sighs.auratip.compat.kubejs.tip;

import cc.sighs.auratip.data.TipData;
import dev.latvian.mods.kubejs.event.KubeEvent;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TipRegistrationEvent implements KubeEvent {

    private final Map<String, TipBuilder> tips = new LinkedHashMap<>();

    public TipBuilder create(String id) {
        TipBuilder builder = new TipBuilder(id);
        tips.put(id, builder);
        return builder;
    }

    public void remove(String id) {
        tips.remove(id);
    }

    public List<TipData> buildAll() {
        List<TipData> result = new ArrayList<>();
        for (TipBuilder builder : tips.values()) {
            result.add(builder.build());
        }
        return result;
    }
}
