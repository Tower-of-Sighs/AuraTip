package cc.sighs.auratip.compat.nekojs.tip;

import cc.sighs.auratip.data.TipData;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TipRegistrationEvent {

    private final Map<String, TipBuilder> tips = new LinkedHashMap<>();
    private final Map<String, TipData> imported = new LinkedHashMap<>();
    private static final Gson GSON = new Gson();

    public TipBuilder create(String id) {
        String key = normalizeId(id);
        if (tips.containsKey(key)) {
            throw new IllegalStateException("Duplicate tip id: " + key);
        }
        if (imported.containsKey(key)) {
            throw new IllegalStateException("Duplicate tip id (already imported): " + key);
        }
        TipBuilder builder = new TipBuilder(key);
        tips.put(key, builder);
        return builder;
    }

    public void remove(String id) {
        String key = normalizeId(id);
        tips.remove(key);
        imported.remove(key);
    }

    public void importJson(Object json) {
        JsonElement element = toJsonElement(json);
        TipData data = TipData.CODEC.parse(JsonOps.INSTANCE, element)
                .resultOrPartial(err -> {
                    throw new IllegalStateException("TipData import failed: " + err);
                })
                .orElseThrow();
        String key = data.id().toString();
        if (tips.containsKey(key) || imported.containsKey(key)) {
            throw new IllegalStateException("Duplicate tip id: " + key);
        }
        imported.put(key, data);
    }

    public List<TipData> buildAll() {
        List<TipData> result = new ArrayList<>(imported.values());
        for (TipBuilder builder : tips.values()) {
            result.add(builder.build());
        }
        return result;
    }

    private static JsonElement toJsonElement(Object json) {
        if (json == null) {
            return JsonNull.INSTANCE;
        }
        if (json instanceof JsonElement e) {
            return e;
        }
        if (json instanceof CharSequence seq) {
            String s = seq.toString();
            try {
                return JsonParser.parseString(s);
            } catch (Throwable ignored) {
                // Fall through to best-effort conversion.
            }
        }
        return GSON.toJsonTree(json);
    }

    private static String normalizeId(String id) {
        if (id == null || id.isEmpty()) {
            return "nekojs:tip";
        }
        if (id.indexOf(':') < 0) {
            return "nekojs:" + id;
        }
        return id;
    }
}
