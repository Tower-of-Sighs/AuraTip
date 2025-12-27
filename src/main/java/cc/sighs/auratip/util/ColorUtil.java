package cc.sighs.auratip.util;

import net.minecraft.util.Mth;

public final class ColorUtil {

    private ColorUtil() {
    }

    public static int lerpColor(int c1, int c2, float t) {
        t = Mth.clamp(t, 0.0f, 1.0f);
        int a1 = (c1 >>> 24) & 0xFF;
        int r1 = (c1 >>> 16) & 0xFF;
        int g1 = (c1 >>> 8) & 0xFF;
        int b1 = c1 & 0xFF;
        int a2 = (c2 >>> 24) & 0xFF;
        int r2 = (c2 >>> 16) & 0xFF;
        int g2 = (c2 >>> 8) & 0xFF;
        int b2 = c2 & 0xFF;
        int a = (int) (a1 + (a2 - a1) * t);
        int r = (int) (r1 + (r2 - r1) * t);
        int g = (int) (g1 + (g2 - g1) * t);
        int b = (int) (b1 + (b2 - b1) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
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

    public static int multiplyAlpha(int argb, float factor) {
        int a = (int) (((argb >>> 24) & 0xFF) * factor);
        return (a << 24) | (argb & 0xFFFFFF);
    }
}