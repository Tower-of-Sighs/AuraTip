package cc.sighs.auratip.util;

import net.minecraft.util.Mth;

public final class AnimationUtil {

    private AnimationUtil() {}

    /**
     * 计算线性动画进度（无缓动效果）
     * @param nowMs    当前时间戳（毫秒）
     * @param startMs  动画开始时间戳（毫秒）
     * @param closing  是否为关闭动画（true：关闭，false：打开）
     * @param openMs   打开动画持续时间（毫秒）
     * @param closeMs  关闭动画持续时间（毫秒）
     * @return         标准化进度值 [0.0, 1.0]
     */
    public static float linearProgress(long nowMs, long startMs, boolean closing,
                                       int openMs, int closeMs) {
        float progress = calculateBaseProgress(nowMs, startMs, closing, openMs, closeMs);
        return Mth.clamp(closing ? 1.0f - progress : progress, 0.0f, 1.0f);
    }

    /**
     * 计算平滑缓动动画进度（使用 smoothstep 函数：t²(3-2t)）
     * @param nowMs    当前时间戳（毫秒）
     * @param startMs  动画开始时间戳（毫秒）
     * @param closing  是否为关闭动画（true：关闭，false：打开）
     * @param openMs   打开动画持续时间（毫秒）
     * @param closeMs  关闭动画持续时间（毫秒）
     * @return         应用缓动函数后的标准化进度值 [0.0, 1.0]
     */
    public static float smoothProgress(long nowMs, long startMs, boolean closing,
                                       int openMs, int closeMs) {
        float t = calculateBaseProgress(nowMs, startMs, closing, openMs, closeMs);
        float progress = closing ? 1.0f - t : t;
        return progress * progress * (3.0f - 2.0f * progress);
    }

    /**
     * 计算基础动画进度
     */
    private static float calculateBaseProgress(long nowMs, long startMs, boolean closing,
                                               int openMs, int closeMs) {
        int duration = closing ? closeMs : openMs;

        if (duration <= 0) {
            return closing ? 1.0f : 0.0f;
        }

        float elapsed = (nowMs - startMs) / (float) duration;
        return Mth.clamp(elapsed, 0.0f, 1.0f);
    }
}