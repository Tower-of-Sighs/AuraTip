package cc.sighs.auratip.data.animation.ha;

import cc.sighs.auratip.data.animation.Animation;

public interface HoverAnimation extends Animation {
    int offsetX(long nowMs, long startMs, int panelWidth, int panelHeight, float speed);

    int offsetY(long nowMs, long startMs, int panelWidth, int panelHeight, float speed);
}

