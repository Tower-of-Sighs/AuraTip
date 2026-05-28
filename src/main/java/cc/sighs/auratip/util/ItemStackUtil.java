package cc.sighs.auratip.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.ItemStack;

/**
 * Utility for converting ItemStack JSON to {@link ItemStack} objects.
 * <p>
 * Used primarily by code generation exports (Java / KubeJS) to reconstruct item
 * templates from editor data, including DataComponentPatch.
 */
public final class ItemStackUtil {

    private ItemStackUtil() {
    }

    /**
     * Parses a JSON string into an {@link ItemStack}.
     * <p>
     * Expected format:
     * <pre>{@code {"id": "minecraft:potion", "count": 1, "components": {...}}}</pre>
     *
     * @param json the ItemStack JSON
     * @return parsed ItemStack
     * @throws RuntimeException if parsing fails
     */
    public static ItemStack fromJson(String json) {
        JsonElement element = JsonParser.parseString(json);
        return ItemStack.CODEC.parse(JsonOps.INSTANCE, element)
                .getOrThrow();
    }

    /**
     * Parses a {@link JsonElement} into an {@link ItemStack}.
     */
    public static ItemStack fromJsonElement(JsonElement json) {
        return ItemStack.CODEC.parse(JsonOps.INSTANCE, json)
                .getOrThrow();
    }
}
