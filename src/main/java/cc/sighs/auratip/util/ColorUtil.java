package cc.sighs.auratip.util;

import net.minecraft.util.Mth;

public final class ColorUtil {

    private ColorUtil() {
    }

    public static int parseRgb(String hex) {
        if (hex == null || hex.isBlank()) {
            return 0xFFFFFF;
        }
        var v = hex.charAt(0) == '#' ? hex.substring(1) : hex;
        try {
            long value = Long.parseLong(v, 16);
            return (int) (value & 0xFFFFFFL);
        } catch (NumberFormatException e) {
            return 0xFFFFFF;
        }
    }

    public static int parseArgb(String hex) {
        if (hex == null || hex.isBlank()) {
            return 0xFF101622;
        }
        var v = hex.charAt(0) == '#' ? hex.substring(1) : hex;
        try {
            long value = Long.parseLong(v, 16);
            if (v.length() <= 6) {
                return (int) (0xFF000000L | (value & 0xFFFFFFL));
            } else {
                return (int) (value & 0xFFFFFFFFL);
            }
        } catch (NumberFormatException e) {
            return 0xFF101622;
        }
    }

    public static int withAlpha(int rgb, int alpha) {
        alpha = Mth.clamp(alpha, 0, 255);
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }
}