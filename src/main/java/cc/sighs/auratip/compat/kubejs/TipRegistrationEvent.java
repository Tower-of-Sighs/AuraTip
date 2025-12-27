package cc.sighs.auratip.compat.kubejs;

import cc.sighs.auratip.data.TipData;
import dev.latvian.mods.kubejs.event.EventJS;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TipRegistrationEvent extends EventJS {

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
