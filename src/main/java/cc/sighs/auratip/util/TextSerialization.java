package cc.sighs.auratip.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.Map;
import java.util.Optional;

public final class TextSerialization {
    private TextSerialization() {
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, Map<String, Component>> VARIABLES_CODEC =
            ByteBufCodecs.fromCodecWithRegistriesTrusted(Codec.unboundedMap(Codec.STRING, ComponentSerialization.CODEC));

    public record TextElement(Component text, float scale, int lineSpacing, Optional<Divider> divider) {
        public static final Codec<TextElement> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        ComponentSerialization.CODEC.fieldOf("text").forGetter(TextElement::text),
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

    public record Divider(int thickness, int marginTop, int marginBottom, float length, String color) {
        public static final Codec<Divider> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.INT.optionalFieldOf("thickness", 1).forGetter(Divider::thickness),
                        Codec.INT.optionalFieldOf("margin_top", 4).forGetter(Divider::marginTop),
                        Codec.INT.optionalFieldOf("margin_bottom", 4).forGetter(Divider::marginBottom),
                        Codec.FLOAT.optionalFieldOf("length", 1.0f).forGetter(Divider::length),
                        Codec.STRING.optionalFieldOf("color")
                                .xmap(
                                        opt -> opt.orElse(""),
                                        value -> value == null || value.isBlank() ? Optional.empty() : Optional.of(value)
                                )
                                .forGetter(Divider::color)
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
            if (color == null) {
                color = "";
            }
        }
    }
}
