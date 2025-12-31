package cc.sighs.auratip.compat.kubejs.tip;

import cc.sighs.auratip.util.ColorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.Locale;

public class TipText {
    public static Builder of(Object base) {
        if (base instanceof Component component) {
            return new Builder(component);
        }
        return new Builder(Component.literal(String.valueOf(base)));
    }

    public static Builder translatable(String key, Object... args) {
        return new Builder(Component.translatable(key, args));
    }

    public static Component join(Object... parts) {
        MutableComponent out = Component.empty();
        if (parts == null) {
            return out;
        }
        for (Object p : parts) {
            if (p == null) continue;
            if (p instanceof Builder b) {
                out.append(b.build());
            } else if (p instanceof Component c) {
                out.append(c);
            } else {
                out.append(Component.literal(String.valueOf(p)));
            }
        }
        return out;
    }

    public static final class Builder {
        private final MutableComponent component;

        private Builder(Component base) {
            this.component = base.copy();
        }

        public Builder colorHex(String hex) {
            Style style = styleFromHex(hex).applyTo(component.getStyle());
            component.setStyle(style);
            return this;
        }

        public Builder colorRgb(int rgb) {
            TextColor color = TextColor.fromRgb(rgb & 0xFFFFFF);
            Style style = component.getStyle().withColor(color);
            component.setStyle(style);
            return this;
        }

        public Builder formatting(String formattingName) {
            if (formattingName == null || formattingName.isBlank()) {
                return this;
            }
            ChatFormatting formatting;
            try {
                formatting = ChatFormatting.valueOf(formattingName.toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                return this;
            }
            Style style = component.getStyle().applyFormat(formatting);
            component.setStyle(style);
            return this;
        }

        public Builder bold() {
            Style style = component.getStyle().applyFormat(ChatFormatting.BOLD);
            component.setStyle(style);
            return this;
        }

        public Builder italic() {
            Style style = component.getStyle().applyFormat(ChatFormatting.ITALIC);
            component.setStyle(style);
            return this;
        }

        public Builder underlined() {
            Style style = component.getStyle().applyFormat(ChatFormatting.UNDERLINE);
            component.setStyle(style);
            return this;
        }

        public Builder strikethrough() {
            Style style = component.getStyle().applyFormat(ChatFormatting.STRIKETHROUGH);
            component.setStyle(style);
            return this;
        }

        public Builder obfuscated() {
            Style style = component.getStyle().applyFormat(ChatFormatting.OBFUSCATED);
            component.setStyle(style);
            return this;
        }

        public Builder appendText(String text) {
            if (text != null && !text.isEmpty()) {
                component.append(text);
            }
            return this;
        }

        public Builder appendComponent(Component other) {
            if (other != null) {
                component.append(other);
            }
            return this;
        }

        public Component build() {
            return component;
        }
    }

    private static Style styleFromHex(String hex) {
        if (hex == null || hex.isBlank()) {
            return Style.EMPTY;
        }
        int argb = ColorUtil.parseArgb(hex);
        int rgb = argb & 0xFFFFFF;
        TextColor color = TextColor.fromRgb(rgb);
        return Style.EMPTY.withColor(color);
    }
}