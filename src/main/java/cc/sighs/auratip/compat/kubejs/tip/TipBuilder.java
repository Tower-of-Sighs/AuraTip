package cc.sighs.auratip.compat.kubejs.tip;

import cc.sighs.auratip.data.TipData;
import cc.sighs.auratip.util.ComponentSerialization;
import cc.sighs.auratip.util.SerializationUtil;
import com.mojang.serialization.Dynamic;
import net.minecraft.network.chat.Component;

import java.util.*;
import java.util.function.Consumer;

public class TipBuilder {

    private final String id;
    private final Map<Integer, PageData> pages = new LinkedHashMap<>();
    private String triggerType = "FIRST_JOIN_WORLD";
    private TipData.Trigger.Mode triggerMode = TipData.Trigger.Mode.ONCE;
    private int triggerCooldown;
    private String animationStyle = "fade_and_slide";
    private float animationSpeed = 1.0f;
    private final Map<String, Object> animationParams = new HashMap<>();
    private final Map<String, Object> hoverAnimationParams = new HashMap<>();
    private String hoverAnimationStyle = "none";
    private float hoverAnimationSpeed = 1.0f;
    private boolean hoverOnlyOnHover;
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

    public TipBuilder(String id) {
        this.id = id;
    }

    public TipBuilder trigger(String type, String mode, int cooldown) {
        this.triggerType = type;
        if (mode != null) {
            try {
                this.triggerMode = TipData.Trigger.Mode.valueOf(mode.toUpperCase());
            } catch (IllegalArgumentException ignored) {
                this.triggerMode = TipData.Trigger.Mode.ONCE;
            }
        }
        this.triggerCooldown = Math.max(0, cooldown);
        return this;
    }

    public TipBuilder visual(Consumer<VisualBuilder> visual) {
        VisualBuilder builder = new VisualBuilder();
        visual.accept(builder);
        return this;
    }

    public TipBuilder behavior(Consumer<BehaviorBuilder> behavior) {
        BehaviorBuilder builder = new BehaviorBuilder();
        behavior.accept(builder);
        return this;
    }

    public TipBuilder page(int index, Consumer<PageBuilder> page) {
        PageData data = pages.computeIfAbsent(index, i -> new PageData());
        PageBuilder builder = new PageBuilder(data);
        page.accept(builder);
        return this;
    }

    TipData build() {
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

    public static class PageData {
        ComponentSerialization.TextElement title;
        ComponentSerialization.TextElement subtitle;
        ComponentSerialization.TextElement content;
        TipData.ImageElement image;
    }

    public static class PageBuilder {
        private final PageData data;

        public PageBuilder(PageData data) {
            this.data = data;
        }

        public PageBuilder title(Component text, float scale, int spacing) {
            data.title = new ComponentSerialization.TextElement(text, scale, spacing, Optional.empty());
            return this;
        }

        public PageBuilder subtitle(Component text, float scale, int spacing) {
            data.subtitle = new ComponentSerialization.TextElement(text, scale, spacing, Optional.empty());
            return this;
        }

        public PageBuilder content(Component text, float scale, int spacing) {
            data.content = new ComponentSerialization.TextElement(text, scale, spacing, Optional.empty());
            return this;
        }

        public PageBuilder titleDivider(int thickness, int marginTop, int marginBottom, float length, String colorHex) {
            data.title = new ComponentSerialization.TextElement(
                    data.title != null ? data.title.text() : Component.empty(),
                    data.title != null ? data.title.scale() : 1.0f,
                    data.title != null ? data.title.lineSpacing() : 0,
                    Optional.of(new ComponentSerialization.Divider(thickness, marginTop, marginBottom, length, colorHex == null ? "" : colorHex))
            );
            return this;
        }

        public PageBuilder titleDivider(int thickness, int marginTop, int marginBottom) {
            return titleDivider(thickness, marginTop, marginBottom, 1.0f, null);
        }

        public PageBuilder titleDivider() {
            return titleDivider(1, 4, 4, 1.0f, null);
        }

        public PageBuilder image(String path, String position, int width, int height) {
            TipData.Position pos = new TipData.Position(position, 0, 0, false);
            data.image = new TipData.ImageElement(path, pos, new int[]{width, height}, 1.0f);
            return this;
        }

        public PageBuilder image(String path, int x, int y, int width, int height) {
            TipData.Position pos = new TipData.Position(null, x, y, true);
            data.image = new TipData.ImageElement(path, pos, new int[]{width, height}, 1.0f);
            return this;
        }

        public PageBuilder imageScaled(String path, String position, int width, int height, float scale) {
            TipData.Position pos = new TipData.Position(position, 0, 0, false);
            data.image = new TipData.ImageElement(path, pos, new int[]{width, height}, scale);
            return this;
        }

        public PageBuilder imageScaled(String path, int x, int y, int width, int height, float scale) {
            TipData.Position pos = new TipData.Position(null, x, y, true);
            data.image = new TipData.ImageElement(path, pos, new int[]{width, height}, scale);
            return this;
        }
    }

    public class VisualBuilder {
        public VisualBuilder animationStyle(String style) {
            animationStyle = style;
            return this;
        }

        public VisualBuilder animationSpeed(float speed) {
            animationSpeed = speed;
            return this;
        }

        public VisualBuilder hoverAnimationStyle(String style) {
            hoverAnimationStyle = style;
            return this;
        }

        public VisualBuilder hoverAnimationSpeed(float speed) {
            hoverAnimationSpeed = speed;
            return this;
        }

        public VisualBuilder hoverOnlyOnHover(boolean value) {
            hoverOnlyOnHover = value;
            return this;
        }

        public VisualBuilder animParam(String key, Object value) {
            animationParams.put(key, value);
            return this;
        }

        public VisualBuilder animParams(Map<String, Object> params) {
            animationParams.putAll(params);
            return this;
        }

        public VisualBuilder hoverParam(String key, Object value) {
            hoverAnimationParams.put(key, value);
            return this;
        }

        public VisualBuilder hoverParams(Map<String, Object> params) {
            hoverAnimationParams.putAll(params);
            return this;
        }

        public VisualBuilder themeColor(String hex) {
            themeColor = hex;
            return this;
        }

        public VisualBuilder size(int w, int h) {
            width = w;
            height = h;
            return this;
        }

        public VisualBuilder position(String pos) {
            position = new TipData.Position(pos, 0, 0, false);
            return this;
        }

        public VisualBuilder position(int x, int y) {
            position = new TipData.Position(null, x, y, true);
            return this;
        }

        public VisualBuilder animationFrom(String pos) {
            animationFrom = new TipData.Position(pos, 0, 0, false);
            return this;
        }

        public VisualBuilder animationFrom(int x, int y) {
            animationFrom = new TipData.Position(null, x, y, true);
            return this;
        }

        public VisualBuilder animationTo(String pos) {
            animationTo = new TipData.Position(pos, 0, 0, false);
            return this;
        }

        public VisualBuilder animationTo(int x, int y) {
            animationTo = new TipData.Position(null, x, y, true);
            return this;
        }

        public VisualBuilder background(String type, List<String> colors, int radius) {
            if (type != null) {
                try {
                    backgroundType = TipData.VisualSettings.BackgroundType.valueOf(type.toUpperCase());
                } catch (IllegalArgumentException ignored) {
                    backgroundType = TipData.VisualSettings.BackgroundType.GRADIENT;
                }
            }
            backgroundColors = colors == null ? new ArrayList<>() : new ArrayList<>(colors);
            backgroundRadius = radius;
            return this;
        }

        public VisualBuilder backgroundRounded(boolean value) {
            backgroundRounded = value;
            return this;
        }

        public VisualBuilder backgroundImage(String path) {
            backgroundImagePath = path;
            return this;
        }
    }

    public class BehaviorBuilder {
        public BehaviorBuilder duration(int ticks) {
            defaultDuration = ticks;
            return this;
        }

        public BehaviorBuilder pauseOnHover(boolean pause) {
            pauseOnHover = pause;
            return this;
        }

        public BehaviorBuilder closeKey(String key) {
            closeKey = key;
            return this;
        }

        public BehaviorBuilder allowPaging(boolean allow) {
            allowPaging = allow;
            return this;
        }
    }
}
