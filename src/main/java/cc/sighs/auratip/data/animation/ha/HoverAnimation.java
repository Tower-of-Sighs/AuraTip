package cc.sighs.auratip.data.animation.ha;

import cc.sighs.auratip.data.animation.Animation;

public interface HoverAnimation extends Animation {

    /**
     * 计算当前时间点下 X 方向的像素偏移量。
     * <p>
     * 此方法在每一帧渲染时被调用，用于动态调整提示框的水平位置。
     *
     * @param nowMs       当前系统时间戳（毫秒）
     * @param startMs     悬停开始时间戳（毫秒）
     * @param panelWidth  提示框面板的宽度（像素）
     * @param panelHeight 提示框面板的高度（像素）
     * @param speed       动画速度系数（由配置控制，>0；值越大动画越快）
     * @return X 方向偏移量（像素），正数表示向右偏移
     */
    int offsetX(long nowMs, long startMs, int panelWidth, int panelHeight, float speed);

    /**
     * 计算当前时间点下 Y 方向的像素偏移量。
     * <p>
     * 此方法在每一帧渲染时被调用，用于动态调整提示框的垂直位置。
     *
     * @param nowMs       当前系统时间戳（毫秒）
     * @param startMs     悬停开始时间戳（毫秒）
     * @param panelWidth  提示框面板的宽度（像素）
     * @param panelHeight 提示框面板的高度（像素）
     * @param speed       动画速度系数（由配置控制，>0；值越大动画越快）
     * @return Y 方向偏移量（像素），正数表示向下偏移
     */
    int offsetY(long nowMs, long startMs, int panelWidth, int panelHeight, float speed);
}

