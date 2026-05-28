package cc.sighs.auratip.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.ItemStackTemplate;

/**
 * Utility for converting ItemStackTemplate JSON to {@link ItemStackTemplate} objects.
 * <p>
 * Used primarily by code generation exports (Java / NekoJS) to reconstruct item
 * templates from editor data, including DataComponentPatch.
 */
public final class ItemStackTemplateUtil {

    private ItemStackTemplateUtil() {
    }

    /**
     * Parses a JSON string into an {@link ItemStackTemplate}.
     * <p>
     * Expected format:
     * <pre>{@code {"id": "minecraft:potion", "count": 1, "components": {...}}}</pre>
     *
     * @param json the ItemStackTemplate JSON
     * @return parsed ItemStackTemplate
     * @throws RuntimeException if parsing fails
     */
    public static ItemStackTemplate fromJson(String json) {
        JsonElement element = JsonParser.parseString(json);
        return ItemStackTemplate.CODEC.parse(JsonOps.INSTANCE, element)
                .getOrThrow();
    }

    /**
     * Parses a {@link JsonElement} into an {@link ItemStackTemplate}.
     */
    public static ItemStackTemplate fromJsonElement(JsonElement json) {
        return ItemStackTemplate.CODEC.parse(JsonOps.INSTANCE, json)
                .getOrThrow();
    }
}
