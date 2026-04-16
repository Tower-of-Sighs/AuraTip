package cc.sighs.auratip.api.animation;

/**
 * Hover animation for tips.
 * <p>
 * Implementations are called every frame while the tip is visible (and depending on configuration, only when
 * the mouse is hovering the panel).
 */
public interface HoverAnimation extends Animation {

    /**
     * @return x offset in pixels
     */
    int offsetX(long nowMs, long startMs, int panelWidth, int panelHeight, float speed);

    /**
     * @return y offset in pixels
     */
    int offsetY(long nowMs, long startMs, int panelWidth, int panelHeight, float speed);
}

