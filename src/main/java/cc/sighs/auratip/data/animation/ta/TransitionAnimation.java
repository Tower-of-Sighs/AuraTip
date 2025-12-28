package cc.sighs.auratip.data.animation.ta;

import cc.sighs.auratip.data.animation.Animation;

public interface TransitionAnimation extends Animation {
    /**
     * 计算在当前时间点上的平滑进度 [0,1]，内部处理开关时长和收起方向
     */
    float easedProgress(long nowMs, long startMs, boolean closing, int openMs, int closeMs);

    /**
     * 根据平滑进度计算 X 方向偏移（像素）
     */
    int offsetX(float eased, int panelWidth, int panelHeight);

    /**
     * 根据平滑进度计算 Y 方向偏移（像素）
     */
    int offsetY(float eased, int panelWidth, int panelHeight);
}
