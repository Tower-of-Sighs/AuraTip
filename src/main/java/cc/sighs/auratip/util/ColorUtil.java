package cc.sighs.auratip.util;

import net.minecraft.util.Mth;

public final class ColorUtil {

    private ColorUtil() {
    }

    /**
     * 在两个 ARGB 颜色之间进行线性插值。
     *
     * @param c1 起始颜色（ARGB 格式）
     * @param c2 目标颜色（ARGB 格式）
     * @param t  插值因子，范围 [0.0, 1.0]；0.0 返回 c1，1.0 返回 c2
     * @return   插值后的 ARGB 颜色值
     */
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

    /**
     * 解析十六进制 RGB 字符串为不带 Alpha 通道的颜色值（默认 Alpha=0xFF）。
     * 支持带或不带 '#' 前缀的格式（如 "#FF5500" 或 "FF5500"）。
     * 若输入无效，则返回白色（0xFFFFFF）。
     *
     * @param hex 十六进制颜色字符串（可为 null 或空白）
     * @return    RGB 颜色值
     */
    public static int parseRgb(String hex) {
        if (hex == null || hex.isBlank()) {
            return 0xFFFFFF;
        }
        String v = hex.charAt(0) == '#' ? hex.substring(1) : hex;
        try {
            long value = Long.parseLong(v, 16);
            return (int) (value & 0xFFFFFFL);
        } catch (NumberFormatException e) {
            return 0xFFFFFF;
        }
    }

    /**
     * 解析十六进制 ARGB/RGB 字符串为带 Alpha 通道的颜色值。
     * - 若字符串长度 ≤ 6（如 "FF5500"），视为 RGB，Alpha 默认为 0xFF。
     * - 若长度为 8（如 "80FF5500"），视为 ARGB。
     * 支持带或不带 '#' 前缀。
     * 若输入无效，则返回默认深色（0xFF101622）。
     *
     * @param hex 十六进制颜色字符串（可为 null 或空白）
     * @return    ARGB 颜色值
     */
    public static int parseArgb(String hex) {
        if (hex == null || hex.isBlank()) {
            return 0xFF101622;
        }
        String v = hex.charAt(0) == '#' ? hex.substring(1) : hex;
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

    /**
     * 为 RGB 颜色设置指定的透明度。
     *
     * @param rgb   RGB 颜色值（低 24 位有效）
     * @param alpha Alpha 通道值，范围 [0, 255]
     * @return      ARGB 颜色值
     */
    public static int withAlpha(int rgb, int alpha) {
        alpha = Mth.clamp(alpha, 0, 255);
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }

    /**
     * 按比例缩放颜色的 Alpha 通道。
     *
     * @param argb   输入的 ARGB 颜色值
     * @param factor 缩放因子，建议范围 [0.0, 1.0]（超出会 clamp 到 [0,255]）
     * @return       Alpha 被缩放后的 ARGB 颜色值
     */
    public static int multiplyAlpha(int argb, float factor) {
        int a = (int) (((argb >>> 24) & 0xFF) * factor);
        a = Mth.clamp(a, 0, 255);
        return (a << 24) | (argb & 0xFFFFFF);
    }
}