package cc.sighs.auratip.api.util;

import cc.sighs.auratip.util.SerializationUtil;
import com.mojang.serialization.Dynamic;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Map;

/**
 * Typed accessors for {@code Map<String, Dynamic<?>>} parameters.
 */
public record Params(Map<String, Dynamic<?>> raw) {

    public Params(@Nullable Map<String, Dynamic<?>> raw) {
        this.raw = (raw == null || raw.isEmpty()) ? Collections.emptyMap() : raw;
    }

    public double getDouble(String key, double fallback) {
        return SerializationUtil.getDouble(raw, key, fallback);
    }

    public float getFloat(String key, float fallback) {
        return SerializationUtil.getFloat(raw, key, fallback);
    }

    public int getInt(String key, int fallback) {
        return SerializationUtil.getInt(raw, key, fallback);
    }

    public long getLong(String key, long fallback) {
        return SerializationUtil.getLong(raw, key, fallback);
    }

    public boolean getBoolean(String key, boolean fallback) {
        return SerializationUtil.getBoolean(raw, key, fallback);
    }

    public String getString(String key, String fallback) {
        return SerializationUtil.getString(raw, key, fallback);
    }
}
