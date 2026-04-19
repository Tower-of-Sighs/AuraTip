package cc.sighs.auratip.api.tip;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.data.TipData;
import cc.sighs.auratip.util.TextSerialization;
import cc.sighs.auratip.util.SerializationUtil;
import com.mojang.serialization.Dynamic;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.*;
import java.util.function.Consumer;

/**
 * Builds {@link TipData}.
 * <p>
 * Text is provided as vanilla {@link Component}. If your text contains <code>${key}</code> placeholders, pass a
 * variable map when sending/triggering tips (see {@link TipServer}).
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class TipBuilder {

    private final ResourceLocation id;
    private final Map<Integer, PageData> pages = new LinkedHashMap<>();

    private ResourceLocation triggerType = AuraTip.id("first_join_world");
    private TipData.Trigger.Mode triggerMode = TipData.Trigger.Mode.ONCE;
    private int triggerCooldown;

    private ResourceLocation animationStyle = AuraTip.id("fade_and_slide");
    private float animationSpeed = 1.0f;
    private final Map<String, Object> animationParams = new HashMap<>();

    private final Map<String, Object> hoverAnimationParams = new HashMap<>();
    private ResourceLocation hoverAnimationStyle = AuraTip.id("none");
    private float hoverAnimationSpeed = 1.0f;
    private boolean hoverOnlyOnHover;

    private int stripeWidth = 4;
    private float stripeLengthFactor = 1.0f;

    private String themeColor;

    private int width = 280;
    private int height = 180;
    private TipData.Position position = new TipData.Position("BOTTOM_CENTER", 0, 0, false);
    private TipData.Position animationFrom;
    private TipData.Position animationTo;

    private TipData.VisualSettings.BackgroundType backgroundType = TipData.VisualSettings.BackgroundType.GRADIENT;
    private List<String> backgroundColors = new ArrayList<>();
    private int backgroundRadius = 8;
    private boolean backgroundRounded = true;
    private String backgroundImagePath;

    private int defaultDuration = 200;
    private boolean pauseOnHover = true;
    private String closeKey;
    private boolean allowPaging = true;

    /**
     * Creates a new tip builder.
     *
     * @param id unique tip id. This is used for trigger bookkeeping (e.g. "ONCE" mode) and should be stable.
     */
    public TipBuilder(ResourceLocation id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    /**
     * Sets the trigger for this tip.
     *
     * @param type          trigger type id
     * @param mode          trigger mode (ONCE / REPEATABLE). If {@code null}, defaults to {@link TipData.Trigger.Mode#ONCE}.
     * @param cooldownTicks cooldown in ticks; only applies when {@code mode=REPEATABLE}. Values &lt; 0 are treated as 0.
     * @return this builder
     */
    public TipBuilder trigger(ResourceLocation type, TipData.Trigger.Mode mode, int cooldownTicks) {
        this.triggerType = Objects.requireNonNull(type, "type");
        this.triggerMode = mode == null ? TipData.Trigger.Mode.ONCE : mode;
        this.triggerCooldown = Math.max(0, cooldownTicks);
        return this;
    }

    /**
     * Convenience overload: {@code trigger(type, ONCE, 0)}.
     *
     * @param type trigger type id
     * @return this builder
     */
    public TipBuilder triggerOnce(ResourceLocation type) {
        return trigger(type, TipData.Trigger.Mode.ONCE, 0);
    }

    /**
     * Convenience overload: {@code trigger(type, REPEATABLE, cooldownTicks)}.
     *
     * @param type          trigger type id
     * @param cooldownTicks cooldown in ticks (values &lt; 0 become 0)
     * @return this builder
     */
    public TipBuilder triggerRepeatable(ResourceLocation type, int cooldownTicks) {
        return trigger(type, TipData.Trigger.Mode.REPEATABLE, cooldownTicks);
    }

    /**
     * Configures visual settings, such as size, position, background, animations, etc.
     *
     * @param visual configuration callback
     * @return this builder
     */
    public TipBuilder visual(Consumer<VisualBuilder> visual) {
        if (visual == null) {
            return this;
        }
        VisualBuilder builder = new VisualBuilder();
        visual.accept(builder);
        return this;
    }

    /**
     * Configures behavior settings, such as duration and how the tip can be closed.
     *
     * @param behavior configuration callback
     * @return this builder
     */
    public TipBuilder behavior(Consumer<BehaviorBuilder> behavior) {
        if (behavior == null) {
            return this;
        }
        BehaviorBuilder builder = new BehaviorBuilder();
        behavior.accept(builder);
        return this;
    }

    /**
     * Creates or updates a page.
     *
     * @param index page index, must be unique across pages
     * @param page  configuration callback
     * @return this builder
     */
    public TipBuilder page(int index, Consumer<PageBuilder> page) {
        if (page == null) {
            return this;
        }
        PageData data = pages.computeIfAbsent(index, i -> new PageData());
        PageBuilder builder = new PageBuilder(data);
        page.accept(builder);
        return this;
    }

    /**
     * Builds the final {@link TipData}.
     * <p>
     * Note: this method does not automatically register the tip. Use {@link TipRegistry} to provide runtime tips,
     * or ship the tip as a datapack JSON under {@code data/auratip/tips/...} (AuraTip currently only loads its own namespace).
     *
     * @return built tip data
     */
    public TipData build() {
        if (pages.isEmpty()) {
            throw new IllegalStateException("Tip '" + id + "' has no pages. TipData.pages must contain at least one page.");
        }

        TipData.Trigger trigger = new TipData.Trigger(triggerType, triggerMode, triggerCooldown);

        List<String> colors = backgroundColors.isEmpty()
                ? List.of("#E0F7FF", "#B3E5FC")
                : new ArrayList<>(backgroundColors);

        TipData.VisualSettings.Background bg = new TipData.VisualSettings.Background(
                backgroundType,
                colors,
                backgroundRadius,
                backgroundRounded,
                Optional.ofNullable(backgroundImagePath)
        );

        Map<String, Dynamic<?>> convertedAnimParams = SerializationUtil.convertMapToDynamic(animationParams);
        Map<String, Dynamic<?>> convertedHoverParams = SerializationUtil.convertMapToDynamic(hoverAnimationParams);

        TipData.VisualSettings visual = new TipData.VisualSettings(
                animationStyle,
                bg,
                Optional.ofNullable(themeColor),
                width,
                height,
                position,
                animationSpeed,
                Optional.ofNullable(animationFrom),
                Optional.ofNullable(animationTo),
                hoverAnimationStyle,
                hoverAnimationSpeed,
                hoverOnlyOnHover,
                stripeWidth,
                stripeLengthFactor,
                convertedAnimParams,
                convertedHoverParams
        );

        TipData.Behavior behavior = new TipData.Behavior(
                defaultDuration,
                pauseOnHover,
                Optional.ofNullable(closeKey),
                allowPaging
        );

        List<TipData.Page> pageList = new ArrayList<>();
        for (Map.Entry<Integer, PageData> entry : pages.entrySet()) {
            Integer index = entry.getKey();
            PageData data = entry.getValue();
            boolean hasContent = (data.title != null)
                    || (data.subtitle != null)
                    || (data.content != null)
                    || (data.image != null);
            if (!hasContent) {
                throw new IllegalStateException("Tip '" + id + "' page_index=" + index + " has no content.");
            }
            pageList.add(new TipData.Page(
                    index,
                    Optional.ofNullable(data.title),
                    Optional.ofNullable(data.subtitle),
                    Optional.ofNullable(data.content),
                    Optional.ofNullable(data.image)
            ));
        }

        return new TipData(id, trigger, visual, behavior, pageList);
    }

    /**
     * Mutable page state used by {@link PageBuilder}.
     */
    public static class PageData {
        TextSerialization.TextElement title;
        TextSerialization.TextElement subtitle;
        TextSerialization.TextElement content;
        TipData.ImageElement image;
    }

    /**
     * Builder for a single {@link TipData.Page}.
     * <p>
     * You typically create pages via {@link TipBuilder#page(int, Consumer)}.
     */
    public static class PageBuilder {
        private final PageData data;

        public PageBuilder(PageData data) {
            this.data = Objects.requireNonNull(data, "data");
        }

        /**
         * Sets the page title.
         *
         * @param text         title component
         * @param scale        scale multiplier (&gt; 0 recommended)
         * @param lineSpacing  extra line spacing in pixels
         * @return this builder
         */
        public PageBuilder title(Component text, float scale, int lineSpacing) {
            data.title = new TextSerialization.TextElement(text, scale, lineSpacing, Optional.empty());
            return this;
        }

        /**
         * Convenience overload: {@code scale=1.0, lineSpacing=0}.
         *
         * @param text title component
         * @return this builder
         */
        public PageBuilder title(Component text) {
            return title(text, 1.0f, 0);
        }

        /**
         * Sets the page subtitle.
         */
        public PageBuilder subtitle(Component text, float scale, int lineSpacing) {
            data.subtitle = new TextSerialization.TextElement(text, scale, lineSpacing, Optional.empty());
            return this;
        }

        /**
         * Convenience overload: {@code scale=1.0, lineSpacing=0}.
         */
        public PageBuilder subtitle(Component text) {
            return subtitle(text, 1.0f, 0);
        }

        /**
         * Sets the page content.
         */
        public PageBuilder content(Component text, float scale, int lineSpacing) {
            data.content = new TextSerialization.TextElement(text, scale, lineSpacing, Optional.empty());
            return this;
        }

        /**
         * Convenience overload: {@code scale=1.0, lineSpacing=0}.
         */
        public PageBuilder content(Component text) {
            return content(text, 1.0f, 0);
        }

        /**
         * Adds/updates a divider line under the title.
         * <p>
         * If a title does not exist yet, an empty title is created, because the divider is stored on the title element.
         *
         * @param thickness    divider thickness in pixels
         * @param marginTop    top margin in pixels
         * @param marginBottom bottom margin in pixels
         * @param length       length factor (0~1)
         * @param colorHex     argb hex color string; if null, falls back to empty string (engine default)
         * @return this builder
         */
        public PageBuilder titleDivider(int thickness, int marginTop, int marginBottom, float length, String colorHex) {
            data.title = new TextSerialization.TextElement(
                    data.title != null ? data.title.text() : Component.empty(),
                    data.title != null ? data.title.scale() : 1.0f,
                    data.title != null ? data.title.lineSpacing() : 0,
                    Optional.of(new TextSerialization.Divider(thickness, marginTop, marginBottom, length, colorHex == null ? "" : colorHex))
            );
            return this;
        }

        /**
         * Convenience overload: {@code length=1.0}, {@code colorHex=null}.
         */
        public PageBuilder titleDivider(int thickness, int marginTop, int marginBottom) {
            return titleDivider(thickness, marginTop, marginBottom, 1.0f, null);
        }

        /**
         * Convenience overload: a simple 1px divider with small margins.
         */
        public PageBuilder titleDivider() {
            return titleDivider(1, 4, 4, 1.0f, null);
        }

        /**
         * Adds an image element, positioned using a preset position string (e.g. {@code "TOP_CENTER"}).
         *
         * @param path     texture path (example: {@code minecraft:textures/item/apple.png})
         * @param preset   preset position name
         * @param width    width in pixels
         * @param height   height in pixels
         * @return this builder
         */
        public PageBuilder image(String path, String preset, int width, int height) {
            TipData.Position pos = new TipData.Position(preset, 0, 0, false);
            data.image = new TipData.ImageElement(path, pos, new int[]{width, height}, 1.0f);
            return this;
        }

        /**
         * Adds an image element, positioned using absolute coordinates.
         *
         * @param path   texture path
         * @param x      x coordinate in pixels
         * @param y      y coordinate in pixels
         * @param width  width in pixels
         * @param height height in pixels
         * @return this builder
         */
        public PageBuilder image(String path, int x, int y, int width, int height) {
            TipData.Position pos = new TipData.Position(null, x, y, true);
            data.image = new TipData.ImageElement(path, pos, new int[]{width, height}, 1.0f);
            return this;
        }

        /**
         * Adds an image element with custom scale.
         */
        public PageBuilder imageScaled(String path, String preset, int width, int height, float scale) {
            TipData.Position pos = new TipData.Position(preset, 0, 0, false);
            data.image = new TipData.ImageElement(path, pos, new int[]{width, height}, scale);
            return this;
        }

        /**
         * Adds an image element with custom scale, positioned using absolute coordinates.
         */
        public PageBuilder imageScaled(String path, int x, int y, int width, int height, float scale) {
            TipData.Position pos = new TipData.Position(null, x, y, true);
            data.image = new TipData.ImageElement(path, pos, new int[]{width, height}, scale);
            return this;
        }
    }

    /**
     * Builder for {@link TipData.VisualSettings}.
     * <p>
     * You typically configure this via {@link TipBuilder#visual(Consumer)}.
     */
    public class VisualBuilder {

        /**
         * Sets the transition animation id.
         */
        public VisualBuilder animationStyle(ResourceLocation style) {
            animationStyle = Objects.requireNonNull(style, "style");
            return this;
        }

        /**
         * Sets the transition animation speed multiplier.
         */
        public VisualBuilder animationSpeed(float speed) {
            animationSpeed = speed;
            return this;
        }

        /**
         * Sets the hover animation id.
         */
        public VisualBuilder hoverAnimationStyle(ResourceLocation style) {
            hoverAnimationStyle = Objects.requireNonNull(style, "style");
            return this;
        }

        /**
         * Sets the hover animation speed multiplier.
         */
        public VisualBuilder hoverAnimationSpeed(float speed) {
            hoverAnimationSpeed = speed;
            return this;
        }

        /**
         * Enables hover animation only when the mouse is hovering the panel.
         */
        public VisualBuilder hoverOnlyOnHover(boolean value) {
            hoverOnlyOnHover = value;
            return this;
        }

        /**
         * Sets the theme stripe width in pixels.
         */
        public VisualBuilder stripeWidth(int width) {
            stripeWidth = width;
            return this;
        }

        /**
         * Sets the theme stripe length factor (0~1).
         */
        public VisualBuilder stripeLengthFactor(float factor) {
            stripeLengthFactor = factor;
            return this;
        }

        /**
         * Adds a single parameter for the transition animation.
         * <p>
         * Values are converted to {@link Dynamic} via {@link SerializationUtil}.
         */
        public VisualBuilder animParam(String key, Object value) {
            if (key != null && !key.isEmpty() && value != null) {
                animationParams.put(key, value);
            }
            return this;
        }

        /**
         * Adds multiple parameters for the transition animation.
         */
        public VisualBuilder animParams(Map<String, ?> params) {
            if (params != null && !params.isEmpty()) {
                animationParams.putAll(params);
            }
            return this;
        }

        /**
         * Adds a single parameter for the hover animation.
         */
        public VisualBuilder hoverParam(String key, Object value) {
            if (key != null && !key.isEmpty() && value != null) {
                hoverAnimationParams.put(key, value);
            }
            return this;
        }

        /**
         * Adds multiple parameters for the hover animation.
         */
        public VisualBuilder hoverParams(Map<String, ?> params) {
            if (params != null && !params.isEmpty()) {
                hoverAnimationParams.putAll(params);
            }
            return this;
        }

        /**
         * Sets the theme color.
         *
         * @param argbHex hex string (recommended: {@code "#AARRGGBB"} or {@code "#RRGGBB"}).
         */
        public VisualBuilder themeColor(String argbHex) {
            themeColor = argbHex;
            return this;
        }

        /**
         * Sets the panel size.
         */
        public VisualBuilder size(int w, int h) {
            width = w;
            height = h;
            return this;
        }

        /**
         * Sets the panel position using a preset name.
         *
         * @param preset preset string such as {@code "BOTTOM_CENTER"}
         */
        public VisualBuilder positionPreset(String preset) {
            position = new TipData.Position(preset, 0, 0, false);
            return this;
        }

        /**
         * Sets the panel position using absolute coordinates.
         */
        public VisualBuilder positionAbsolute(int x, int y) {
            position = new TipData.Position(null, x, y, true);
            return this;
        }

        /**
         * Sets the animation start position using a preset name.
         */
        public VisualBuilder animationFromPreset(String preset) {
            animationFrom = new TipData.Position(preset, 0, 0, false);
            return this;
        }

        /**
         * Sets the animation start position using absolute coordinates.
         */
        public VisualBuilder animationFromAbsolute(int x, int y) {
            animationFrom = new TipData.Position(null, x, y, true);
            return this;
        }

        /**
         * Sets the animation end position using a preset name.
         */
        public VisualBuilder animationToPreset(String preset) {
            animationTo = new TipData.Position(preset, 0, 0, false);
            return this;
        }

        /**
         * Sets the animation end position using absolute coordinates.
         */
        public VisualBuilder animationToAbsolute(int x, int y) {
            animationTo = new TipData.Position(null, x, y, true);
            return this;
        }

        /**
         * Sets the background settings.
         *
         * @param type   background type
         * @param colors color list (argb). For GRADIENT you typically provide 2+ colors; for SOLID provide 1 color.
         * @param radius border radius in pixels
         */
        public VisualBuilder background(TipData.VisualSettings.BackgroundType type, List<String> colors, int radius) {
            backgroundType = type == null ? TipData.VisualSettings.BackgroundType.GRADIENT : type;
            backgroundColors = colors == null ? new ArrayList<>() : new ArrayList<>(colors);
            backgroundRadius = radius;
            return this;
        }

        /**
         * Sets whether the background uses rounded corners.
         */
        public VisualBuilder backgroundRounded(boolean value) {
            backgroundRounded = value;
            return this;
        }

        /**
         * Sets the background image path (only used when background type is IMAGE).
         */
        public VisualBuilder backgroundImage(String path) {
            backgroundImagePath = path;
            return this;
        }
    }

    /**
     * Builder for {@link TipData.Behavior}.
     * <p>
     * You typically configure this via {@link TipBuilder#behavior(Consumer)}.
     */
    public class BehaviorBuilder {
        /**
         * Sets the default duration in ticks.
         * <p>
         * Use {@code -1} to make the tip "persistent" until closed.
         */
        public BehaviorBuilder duration(int ticks) {
            defaultDuration = ticks;
            return this;
        }

        /**
         * Sets whether the timer pauses while hovering.
         */
        public BehaviorBuilder pauseOnHover(boolean pause) {
            pauseOnHover = pause;
            return this;
        }

        /**
         * Sets a key binding id which can close the tip (example: {@code "key.keyboard.delete"}).
         */
        public BehaviorBuilder closeKey(String key) {
            closeKey = key;
            return this;
        }

        /**
         * Enables/disables paging (when multiple pages exist).
         */
        public BehaviorBuilder allowPaging(boolean allow) {
            allowPaging = allow;
            return this;
        }
    }

}
