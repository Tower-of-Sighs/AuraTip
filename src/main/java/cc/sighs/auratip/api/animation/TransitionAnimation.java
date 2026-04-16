package cc.sighs.auratip.api.animation;

/**
 * Transition animation for tip open/close.
 */
public interface TransitionAnimation extends Animation {

    /**
     * @return eased progress, usually in range [0, 1]
     */
    float easedProgress(long nowMs, long startMs, boolean closing, int openMs, int closeMs);

    /**
     * @return x offset in pixels for the given eased progress
     */
    int offsetX(float eased, int panelWidth, int panelHeight);

    /**
     * @return y offset in pixels for the given eased progress
     */
    int offsetY(float eased, int panelWidth, int panelHeight);
}

