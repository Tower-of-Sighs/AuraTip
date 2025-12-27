package cc.sighs.auratip.data.animation;

public enum AnimationType {
    FADE_AND_SLIDE(new FadeAndSlideAnimation()),
    FADE(new FadeAnimation()),
    SLIDE(new SlideAnimation()),
    SLIDE_IN_LEFT(new SlideInLeftAnimation()),
    SLIDE_IN_RIGHT(new SlideInRightAnimation()),
    SLIDE_IN_TOP(new SlideInTopAnimation()),
    SLIDE_IN_BOTTOM(new SlideAnimation());

    public static final AnimationType DEFAULT = FADE_AND_SLIDE;

    public final Animation animation;

    AnimationType(Animation animation) {
        this.animation = animation;
    }

    public static AnimationType resolve(String id) {
        if (id == null || id.isBlank()) {
            return DEFAULT;
        }
        try {
            return AnimationType.valueOf(id.trim().toUpperCase());
        } catch (IllegalArgumentException ignored) {
            return DEFAULT;
        }
    }

    public Animation animation() {
        return animation;
    }
}
