package cc.sighs.auratip.data.animation.ta;

import cc.sighs.auratip.data.animation.Animation;

public interface TransitionAnimation extends Animation {

    /**
     * 计算当前时间点下的动画进度。
     * <p>
     * 该进度通常用于控制透明度、缩放或其他线性插值属性。
     *
     * @param nowMs     当前系统时间戳（毫秒）
     * @param startMs   动画开始时间戳（毫秒）
     * @param closing   当前是否处于关闭状态（true 表示正在关闭，false 表示正在打开）
     * @param openMs    配置的打开动画总时长（毫秒）
     * @param closeMs   配置的关闭动画总时长（毫秒）
     * @return          缓动后的进度值，通常在 0.0 到 1.0 之间（允许因弹性效果超出此范围）
     */
    float easedProgress(long nowMs, long startMs, boolean closing, int openMs, int closeMs);

    /**
     * 计算当前进度下 X 方向的像素偏移量。
     *
     * @param eased     由 {@link #easedProgress} 计算出的缓动进度
     * @param panelWidth 提示框面板的宽度（像素）
     * @param panelHeight 提示框面板的高度（像素）
     * @return          X 方向偏移量（像素）
     */
    int offsetX(float eased, int panelWidth, int panelHeight);

    /**
     * 计算当前进度下 Y 方向的像素偏移量。
     *
     * @param eased     由 {@link #easedProgress} 计算出的缓动进度
     * @param panelWidth 提示框面板的宽度（像素）
     * @param panelHeight 提示框面板的高度（像素）
     * @return          Y 方向偏移量（像素）
     */
    int offsetY(float eased, int panelWidth, int panelHeight);
}