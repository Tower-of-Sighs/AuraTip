package cc.sighs.auratip.compat.kubejs;

import cc.sighs.auratip.util.ColorUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

import java.util.Locale;

public final class TipText {
    private TipText() {
    }

    public static Component plain(String text) {
        return Component.literal(text);
    }

    public static Component hex(String text, String hex) {
        Style style = styleFromHex(hex);
        return Component.literal(text).withStyle(style);
    }

    public static Component hexStyled(String text, String hex, boolean bold, boolean italic, boolean underlined) {
        Style style = styleFromHex(hex)
                .withBold(bold)
                .withItalic(italic)
                .withUnderlined(underlined);
        return Component.literal(text).withStyle(style);
    }

    public static Component format(String text, String formattingName) {
        if (formattingName == null || formattingName.isBlank()) {
            return Component.literal(text);
        }
        ChatFormatting formatting;
        try {
            formatting = ChatFormatting.valueOf(formattingName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return Component.literal(text);
        }
        return Component.literal(text).withStyle(formatting);
    }

    public static Component formatBold(String text, String formattingName) {
        if (formattingName == null || formattingName.isBlank()) {
            Style style = Style.EMPTY.withBold(true);
            return Component.literal(text).withStyle(style);
        }
        ChatFormatting formatting;
        try {
            formatting = ChatFormatting.valueOf(formattingName.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            Style style = Style.EMPTY.withBold(true);
            return Component.literal(text).withStyle(style);
        }
        Style style = Style.EMPTY.withColor(formatting).withBold(true);
        return Component.literal(text).withStyle(style);
    }

    public static Component rgb(String text, int rgb) {
        TextColor color = TextColor.fromRgb(rgb & 0xFFFFFF);
        Style style = Style.EMPTY.withColor(color);
        return Component.literal(text).withStyle(style);
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
