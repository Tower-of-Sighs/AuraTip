package cc.sighs.auratip.editor.schema;

import cc.sighs.auratip.AuraTip;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.List;

/**
 * Json schemas for the visual editor UI.
 * <p>
 * The intention is "codec-first": the field names, structure and defaults are based on the
 * corresponding CODEC definitions in {@code TipData} / {@code RadialMenuData}.
 * <p>
 * This is tooling-only and does not affect serialization.
 */
public final class EditorCodecSchemas {
    private EditorCodecSchemas() {
    }

    public static JsonObject buildAll() {
        JsonObject out = new JsonObject();
        out.add("tip", tip());
        out.add("radial", radial());
        return out;
    }

    private static JsonObject tip() {
        return obj(
                f("id", rl(), false, null, "tip.id", "tip.id.desc"),
                f("trigger", tipTrigger(), false, null, "tip.trigger", "tip.trigger.desc"),
                f("visual_settings", tipVisualSettings(), false, null, "tip.visual_settings", "tip.visual_settings.desc"),
                f("behavior", tipBehavior(), false, null, "tip.behavior", "tip.behavior.desc"),
                f("pages", array(tipPage()), false, null, "tip.pages", "tip.pages.desc")
        );
    }

    private static JsonObject tipTrigger() {
        return obj(
                f("type", rl(), false, rlVal(AuraTip.MOD_ID + ":first_join_world"), "tip.trigger.type", "tip.trigger.type.desc"),
                f("mode", enumOf("once", "repeatable"), true, str("once"), "tip.trigger.mode", "tip.trigger.mode.desc"),
                f("cooldown", number(), true, num(0), "tip.trigger.cooldown", "tip.trigger.cooldown.desc")
        );
    }

    private static JsonObject tipVisualSettings() {
        JsonObject bgDefault = new JsonObject();
        bgDefault.addProperty("type", "gradient");
        JsonArray colors = new JsonArray();
        colors.add("#FFE0F7FF");
        colors.add("#FFB3E5FC");
        bgDefault.add("colors", colors);
        bgDefault.addProperty("border_radius", 8);
        bgDefault.addProperty("rounded", true);

        return obj(
                f("animation_style", rl(), true, rlVal(AuraTip.MOD_ID + ":fade_and_slide"), "tip.visual.animation_style", "tip.visual.animation_style.desc"),
                f("background", tipBackground(), true, bgDefault, "tip.visual.background", "tip.visual.background.desc"),
                f("theme_color", string(), true, str("#FFFFFFFF"), "tip.visual.theme_color", "tip.visual.theme_color.desc"),
                f("width", number(), true, num(280), "tip.visual.width", "tip.visual.width.desc"),
                f("height", number(), true, num(180), "tip.visual.height", "tip.visual.height.desc"),
                f("position", position(), true, str("BOTTOM_CENTER"), "tip.visual.position", "tip.visual.position.desc"),
                f("animation_speed", number(), true, num(1.0), "tip.visual.animation_speed", "tip.visual.animation_speed.desc"),
                f("animation_from", position(), true, str("BOTTOM_CENTER"), "tip.visual.animation_from", "tip.visual.animation_from.desc"),
                f("animation_to", position(), true, str("BOTTOM_CENTER"), "tip.visual.animation_to", "tip.visual.animation_to.desc"),
                f("hover_animation_style", rl(), true, rlVal(AuraTip.MOD_ID + ":none"), "tip.visual.hover_animation_style", "tip.visual.hover_animation_style.desc"),
                f("hover_animation_speed", number(), true, num(1.0), "tip.visual.hover_animation_speed", "tip.visual.hover_animation_speed.desc"),
                f("hover_only_on_hover", bool(), true, boolVal(false), "tip.visual.hover_only_on_hover", "tip.visual.hover_only_on_hover.desc"),
                f("stripe_width", number(), true, num(4), "tip.visual.stripe_width", "tip.visual.stripe_width.desc"),
                f("stripe_length_factor", number(), true, num(1.0), "tip.visual.stripe_length_factor", "tip.visual.stripe_length_factor.desc"),
                f("animation_params", dynamicMap(), true, new JsonObject(), "tip.visual.animation_params", "tip.visual.animation_params.desc"),
                f("hover_animation_params", dynamicMap(), true, new JsonObject(), "tip.visual.hover_animation_params", "tip.visual.hover_animation_params.desc")
        );
    }

    private static JsonObject tipBackground() {
        return obj(
                f("type", enumOf("gradient", "solid", "image"), true, str("gradient"), "tip.visual.background.type", "tip.visual.background.type.desc"),
                f("colors", array(string()), true, listStr("#FFE0F7FF", "#FFB3E5FC"), "tip.visual.background.colors", "tip.visual.background.colors.desc"),
                f("border_radius", number(), true, num(8), "tip.visual.background.border_radius", "tip.visual.background.border_radius.desc"),
                f("rounded", bool(), true, boolVal(true), "tip.visual.background.rounded", "tip.visual.background.rounded.desc"),
                f("image_path", string(), true, str("minecraft:textures/block/stone.png"), "tip.visual.background.image_path", "tip.visual.background.image_path.desc")
        );
    }

    private static JsonObject tipBehavior() {
        return obj(
                f("default_duration", number(), true, num(200), "tip.behavior.default_duration", "tip.behavior.default_duration.desc"),
                f("pause_timer_on_hover", bool(), true, boolVal(true), "tip.behavior.pause_timer_on_hover", "tip.behavior.pause_timer_on_hover.desc"),
                f("closable_by_key", string(), true, str("key.keyboard.escape"), "tip.behavior.closable_by_key", "tip.behavior.closable_by_key.desc"),
                f("allow_paging", bool(), true, boolVal(true), "tip.behavior.allow_paging", "tip.behavior.allow_paging.desc")
        );
    }

    private static JsonObject tipPage() {
        JsonObject base = new JsonObject();
        base.addProperty("page_index", 0);
        return obj(
                f("page_index", number(), false, num(0), "tip.page.page_index", "tip.page.page_index.desc"),
                f("title", textElement(), true, defaultTextElement("Title"), "tip.page.title", "tip.page.title.desc"),
                f("subtitle", textElement(), true, defaultTextElement("Subtitle"), "tip.page.subtitle", "tip.page.subtitle.desc"),
                f("content", textElement(), true, defaultTextElement("Content"), "tip.page.content", "tip.page.content.desc"),
                f("image", tipImage(), true, defaultImage(), "tip.page.image", "tip.page.image.desc")
        ).deepCopy();
    }

    private static JsonObject tipImage() {
        JsonArray size = new JsonArray();
        size.add(64);
        size.add(64);

        return obj(
                f("path", string(), false, str("minecraft:textures/item/apple.png"), "tip.page.image.path", "tip.page.image.path.desc"),
                f("position", position(), true, str("TOP_CENTER"), "tip.page.image.position", "tip.page.image.position.desc"),
                f("size", fixedIntArray2(), true, size, "tip.page.image.size", "tip.page.image.size.desc"),
                f("scale", number(), true, num(1.0), "tip.page.image.scale", "tip.page.image.scale.desc")
        );
    }

    private static JsonObject position() {
        // TipData.Position codec is Either<String, List<Int>>; we expose both variants.
        JsonObject preset = new JsonObject();
        preset.addProperty("label", "preset");
        preset.add("type", string());
        JsonObject abs = new JsonObject();
        abs.addProperty("label", "absolute");
        abs.add("type", fixedIntArray2());

        JsonArray variants = new JsonArray();
        variants.add(preset);
        variants.add(abs);

        JsonObject out = new JsonObject();
        out.addProperty("kind", "either");
        out.add("variants", variants);
        return out;
    }

    private static JsonObject textElement() {
        return obj(
                f("text", component(), false, componentLiteral("Text"), "text_element.text", "text_element.text.desc"),
                f("scale", number(), true, num(1.0), "text_element.scale", "text_element.scale.desc"),
                f("line_spacing", number(), true, num(0), "text_element.line_spacing", "text_element.line_spacing.desc"),
                f("divider", divider(), true, defaultDivider(), "text_element.divider", "text_element.divider.desc")
        );
    }

    private static JsonObject divider() {
        return obj(
                f("thickness", number(), true, num(1), "divider.thickness", "divider.thickness.desc"),
                f("margin_top", number(), true, num(4), "divider.margin_top", "divider.margin_top.desc"),
                f("margin_bottom", number(), true, num(4), "divider.margin_bottom", "divider.margin_bottom.desc"),
                f("length", number(), true, num(1.0), "divider.length", "divider.length.desc"),
                f("color", string(), true, str(""), "divider.color", "divider.color.desc")
        );
    }

    private static JsonObject radial() {
        return obj(
                f("id", rl(), false, rlVal(AuraTip.MOD_ID + ":menu"), "radial.id", "radial.id.desc"),
                f("menu_settings", radialSettings(), false, null, "radial.menu_settings", "radial.menu_settings.desc"),
                f("slots", array(radialSlot()), false, null, "radial.slots", "radial.slots.desc")
        );
    }

    private static JsonObject radialSettings() {
        return obj(
                f("inner_radius", number(), false, num(55), "radial.menu_settings.inner_radius", "radial.menu_settings.inner_radius.desc"),
                f("outer_radius", number(), false, num(100), "radial.menu_settings.outer_radius", "radial.menu_settings.outer_radius.desc"),
                f("animation_speed", number(), false, num(1.0), "radial.menu_settings.animation_speed", "radial.menu_settings.animation_speed.desc"),
                f("center_icon", rl(), true, rlVal("minecraft:textures/item/diamond.png"), "radial.menu_settings.center_icon", "radial.menu_settings.center_icon.desc"),
                f("ring_color", string(), true, str("#77FFFFFF"), "radial.menu_settings.ring_color", "radial.menu_settings.ring_color.desc"),
                f("ring_colors", array(string()), true, listStr("#77FFFFFF", "#33FFFFFF"), "radial.menu_settings.ring_colors", "radial.menu_settings.ring_colors.desc")
        );
    }

    private static JsonObject radialSlot() {
        return obj(
                f("name", string(), false, str("Slot"), "radial.slot.name", "radial.slot.name.desc"),
                f("icon", rl(), false, rlVal("minecraft:textures/item/paper.png"), "radial.slot.icon", "radial.slot.icon.desc"),
                f("action", action(), false, actionDefaultRunCommand(), "radial.slot.action", "radial.slot.action.desc"),
                f("text", component(), true, componentLiteral("Slot"), "radial.slot.text", "radial.slot.text.desc"),
                f("highlight_color", string(), true, str("#77FFFFFF"), "radial.slot.highlight_color", "radial.slot.highlight_color.desc")
        );
    }

    private static JsonObject action() {
        JsonObject out = new JsonObject();
        out.addProperty("kind", "action");
        return out;
    }

    // ---- schema helpers ----

    private static JsonObject f(String name, JsonObject type, boolean optional, JsonElement def, String labelKey, String descKey) {
        JsonObject out = new JsonObject();
        out.addProperty("name", name);
        out.add("type", type);
        if (labelKey != null && !labelKey.isBlank()) {
            out.addProperty("label_key", labelKey);
        }
        if (descKey != null && !descKey.isBlank()) {
            out.addProperty("desc_key", descKey);
        }
        if (optional) {
            out.addProperty("optional", true);
        }
        if (def != null) {
            out.add("default", def);
        }
        return out;
    }

    private static JsonObject obj(JsonObject... fields) {
        JsonObject out = new JsonObject();
        out.addProperty("kind", "object");
        JsonArray arr = new JsonArray();
        for (JsonObject f : fields) {
            arr.add(f);
        }
        out.add("fields", arr);
        return out;
    }

    private static JsonObject array(JsonObject itemType) {
        JsonObject out = new JsonObject();
        out.addProperty("kind", "array");
        out.add("item", itemType);
        return out;
    }

    private static JsonObject fixedIntArray2() {
        JsonObject out = new JsonObject();
        out.addProperty("kind", "fixed_int_array2");
        return out;
    }

    private static JsonObject dynamicMap() {
        JsonObject out = new JsonObject();
        out.addProperty("kind", "map_dynamic");
        return out;
    }

    private static JsonObject string() {
        JsonObject out = new JsonObject();
        out.addProperty("kind", "string");
        return out;
    }

    private static JsonObject number() {
        JsonObject out = new JsonObject();
        out.addProperty("kind", "number");
        return out;
    }

    private static JsonObject bool() {
        JsonObject out = new JsonObject();
        out.addProperty("kind", "boolean");
        return out;
    }

    private static JsonObject rl() {
        JsonObject out = new JsonObject();
        out.addProperty("kind", "resource_location");
        return out;
    }

    private static JsonObject component() {
        JsonObject out = new JsonObject();
        out.addProperty("kind", "component");
        return out;
    }

    private static JsonObject enumOf(String... values) {
        JsonObject out = new JsonObject();
        out.addProperty("kind", "enum");
        JsonArray opts = new JsonArray();
        for (String v : values) {
            opts.add(v);
        }
        out.add("options", opts);
        return out;
    }

    private static JsonPrimitive str(String s) {
        return new JsonPrimitive(s == null ? "" : s);
    }

    private static JsonPrimitive boolVal(boolean b) {
        return new JsonPrimitive(b);
    }

    private static JsonPrimitive num(double v) {
        return new JsonPrimitive(v);
    }

    private static JsonPrimitive rlVal(String s) {
        return new JsonPrimitive(s);
    }

    private static JsonArray listStr(String... values) {
        JsonArray arr = new JsonArray();
        for (String v : values) {
            arr.add(v);
        }
        return arr;
    }

    private static JsonObject componentLiteral(String text) {
        JsonObject o = new JsonObject();
        o.addProperty("text", text == null ? "" : text);
        return o;
    }

    private static JsonObject defaultDivider() {
        JsonObject o = new JsonObject();
        o.addProperty("thickness", 1);
        o.addProperty("margin_top", 4);
        o.addProperty("margin_bottom", 4);
        o.addProperty("length", 1.0);
        o.addProperty("color", "");
        return o;
    }

    private static JsonObject defaultTextElement(String text) {
        JsonObject o = new JsonObject();
        o.add("text", componentLiteral(text));
        o.addProperty("scale", 1.0);
        o.addProperty("line_spacing", 0);
        return o;
    }

    private static JsonObject defaultImage() {
        JsonObject o = new JsonObject();
        o.addProperty("path", "minecraft:textures/item/apple.png");
        o.addProperty("position", "TOP_CENTER");
        JsonArray size = new JsonArray();
        size.add(64);
        size.add(64);
        o.add("size", size);
        o.addProperty("scale", 1.0);
        return o;
    }

    private static JsonObject actionDefaultRunCommand() {
        JsonObject o = new JsonObject();
        o.addProperty("type", AuraTip.MOD_ID + ":run_command");
        o.addProperty("command", "/say hello");
        return o;
    }
}
