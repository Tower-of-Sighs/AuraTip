package cc.sighs.auratip.compat.nekojs.radiamenu;

import cc.sighs.auratip.data.RadialMenuData;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RadialMenuRegistrationEvent {

    private final Map<String, RadialMenuBuilder> menus = new LinkedHashMap<>();
    private final Map<String, RadialMenuData> imported = new LinkedHashMap<>();
    private static final Gson GSON = new Gson();

    public RadialMenuBuilder create(String id) {
        String key = normalizeId(id);
        if (menus.containsKey(key)) {
            throw new IllegalStateException("Duplicate radial menu id: " + key);
        }
        if (imported.containsKey(key)) {
            throw new IllegalStateException("Duplicate radial menu id (already imported): " + key);
        }
        RadialMenuBuilder builder = new RadialMenuBuilder(key);
        menus.put(key, builder);
        return builder;
    }

    public void remove(String id) {
        String key = normalizeId(id);
        menus.remove(key);
        imported.remove(key);
    }

    public void importJson(Object json) {
        JsonElement element = toJsonElement(json);
        RadialMenuData data = RadialMenuData.CODEC.parse(JsonOps.INSTANCE, element)
                .resultOrPartial(err -> {
                    throw new IllegalStateException("RadialMenuData import failed: " + err);
                })
                .orElseThrow();
        String key = data.id().toString();
        if (menus.containsKey(key) || imported.containsKey(key)) {
            throw new IllegalStateException("Duplicate radial menu id: " + key);
        }
        imported.put(key, data);
    }

    public List<RadialMenuData> buildAll() {
        List<RadialMenuData> result = new ArrayList<>(imported.values());
        for (RadialMenuBuilder builder : menus.values()) {
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
            return "nekojs:radial_menu";
        }
        if (id.indexOf(':') < 0) {
            return "nekojs:" + id;
        }
        return id;
    }
}
