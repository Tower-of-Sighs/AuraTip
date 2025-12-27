package cc.sighs.auratip.util;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

import java.util.Optional;

public final class ComponentSerialization {
    private ComponentSerialization() {
    }

    public static final Codec<Component> COMPONENT_CODEC = Codec.PASSTHROUGH.xmap(
            dynamic -> {
                JsonElement element = dynamic.convert(JsonOps.INSTANCE).getValue();
                return Component.Serializer.fromJson(element);
            },
            component -> new Dynamic<>(JsonOps.INSTANCE, Component.Serializer.toJsonTree(component))
    );

    public record TextElement(Component text, float scale, int lineSpacing, Optional<Divider> divider) {
        public static final Codec<TextElement> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        ComponentSerialization.COMPONENT_CODEC.fieldOf("text").forGetter(TextElement::text),
                        Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(TextElement::scale),
                        Codec.INT.optionalFieldOf("line_spacing", 0).forGetter(TextElement::lineSpacing),
                        Divider.CODEC.optionalFieldOf("divider").forGetter(TextElement::divider)
                ).apply(inst, TextElement::new)
        );

        public TextElement {
            if (scale <= 0) {
                scale = 1.0f;
            }
            if (lineSpacing < 0) {
                lineSpacing = 0;
            }
        }
    }

    public record Divider(int thickness, int marginTop, int marginBottom, float length, Optional<String> color) {
        public static final Codec<Divider> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.INT.optionalFieldOf("thickness", 1).forGetter(Divider::thickness),
                        Codec.INT.optionalFieldOf("margin_top", 4).forGetter(Divider::marginTop),
                        Codec.INT.optionalFieldOf("margin_bottom", 4).forGetter(Divider::marginBottom),
                        Codec.FLOAT.optionalFieldOf("length", 1.0f).forGetter(Divider::length),
                        Codec.STRING.optionalFieldOf("color").forGetter(Divider::color)
                ).apply(inst, Divider::new)
        );

        public Divider {
            if (thickness <= 0) {
                thickness = 1;
            }
            if (marginTop < 0) {
                marginTop = 0;
            }
            if (marginBottom < 0) {
                marginBottom = 0;
            }
            if (length <= 0.0f) {
                length = 1.0f;
            }
        }
    }
}
