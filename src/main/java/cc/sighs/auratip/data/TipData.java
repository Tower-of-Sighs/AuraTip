package cc.sighs.auratip.data;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.data.validator.TipDataValidator;
import cc.sighs.auratip.util.CodecUtil;
import cc.sighs.auratip.util.ComponentSerialization;
import cc.sighs.oelib.data.api.DataDriven;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@DataDriven(
        modid = AuraTip.MOD_ID,
        folder = "tips",
        syncToClient = true,
        supportArray = true,
        validator = TipDataValidator.class
)
public record TipData(
        ResourceLocation id,
        Trigger trigger,
        VisualSettings visualSettings,
        Behavior behavior,
        List<Page> pages
) {
    public static final Codec<TipData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(TipData::id),
                    Trigger.CODEC.fieldOf("trigger").forGetter(TipData::trigger),
                    VisualSettings.CODEC.fieldOf("visual_settings").forGetter(TipData::visualSettings),
                    Behavior.CODEC.fieldOf("behavior").forGetter(TipData::behavior),
                    Page.CODEC.listOf().fieldOf("pages").forGetter(TipData::pages)
            ).apply(instance, TipData::new)
    );

    public record Position(String preset, int x, int y, boolean absolute) {
        public static final Codec<Position> CODEC = CodecUtil.stringOrIntList(
                p -> new Position(p, 0, 0, false),
                list -> new Position(null, list.isEmpty() ? 0 : list.get(0), list.size() > 1 ? list.get(1) : 0, true),
                pos -> pos.absolute ? null : pos.preset,
                pos -> List.of(pos.x, pos.y)
        );
    }

    public record Padding(int top, int right, int bottom, int left) {
        public static final Padding DEFAULT = new Padding(12, 12, 12, 12);

        private static final Codec<Padding> OBJECT_CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.INT.optionalFieldOf("top", 12).forGetter(Padding::top),
                        Codec.INT.optionalFieldOf("right", 12).forGetter(Padding::right),
                        Codec.INT.optionalFieldOf("bottom", 12).forGetter(Padding::bottom),
                        Codec.INT.optionalFieldOf("left", 12).forGetter(Padding::left)
                ).apply(inst, Padding::new)
        );

        public static final Codec<Padding> CODEC = CodecUtil.intOrListOrObject(
                v -> new Padding(v, v, v, v),
                list -> new Padding(
                        !list.isEmpty() ? list.get(0) : 12,
                        list.size() > 1 ? list.get(1) : 12,
                        list.size() > 2 ? list.get(2) : 12,
                        list.size() > 3 ? list.get(3) : 12
                ),
                padding -> (padding.top == padding.right && padding.top == padding.bottom && padding.top == padding.left) ? padding.top : 0,
                padding -> List.of(padding.top, padding.right, padding.bottom, padding.left),
                OBJECT_CODEC
        );
    }

    public record LayoutConfig(Padding padding, int elementSpacing) {
        public static final LayoutConfig DEFAULT = new LayoutConfig(Padding.DEFAULT, 4);

        public static final Codec<LayoutConfig> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Padding.CODEC.optionalFieldOf("padding", Padding.DEFAULT).forGetter(LayoutConfig::padding),
                        Codec.INT.optionalFieldOf("element_spacing", 4).forGetter(LayoutConfig::elementSpacing)
                ).apply(inst, LayoutConfig::new)
        );
    }

    public record AnimationParams(
            Map<String, Dynamic<?>> params,
            Map<String, Dynamic<?>> hoverParams
    ) {
        public static final Codec<AnimationParams> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.unboundedMap(Codec.STRING, Codec.PASSTHROUGH)
                                .optionalFieldOf("params", Map.of())
                                .forGetter(AnimationParams::params),
                        Codec.unboundedMap(Codec.STRING, Codec.PASSTHROUGH)
                                .optionalFieldOf("hover_params", Map.of())
                                .forGetter(AnimationParams::hoverParams)
                ).apply(inst, AnimationParams::new)
        );

        public static final AnimationParams EMPTY = new AnimationParams(Map.of(), Map.of());
    }

    public record Trigger(ResourceLocation type, Mode mode, int cooldown) {
        public static final Codec<Trigger> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        ResourceLocation.CODEC.fieldOf("type").forGetter(Trigger::type),
                        Mode.CODEC.optionalFieldOf("mode", Mode.ONCE).forGetter(Trigger::mode),
                        Codec.INT.optionalFieldOf("cooldown", 0).forGetter(Trigger::cooldown)
                ).apply(inst, Trigger::new)
        );

        public enum Mode {
            ONCE,
            REPEATABLE;

            public static final Codec<Mode> CODEC = CodecUtil.enumCodec(Mode.class);
        }
    }

    public record VisualSettings(
            ResourceLocation animationStyle,
            Background background,
            Optional<String> themeColor,
            int width,
            int height,
            Position position,
            float animationSpeed,
            Optional<Position> animationFrom,
            Optional<Position> animationTo,
            ResourceLocation hoverAnimationStyle,
            float hoverAnimationSpeed,
            boolean hoverOnlyOnHover,
            int stripeWidth,
            float stripeLengthFactor,
            AnimationParams animationParams,
            LayoutConfig layout
    ) {
        public static final Codec<VisualSettings> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        ResourceLocation.CODEC.optionalFieldOf("animation_style", new ResourceLocation(AuraTip.MOD_ID, "fade_and_slide"))
                                .forGetter(VisualSettings::animationStyle),
                        Background.CODEC.optionalFieldOf("background", new Background(BackgroundType.GRADIENT, List.of("#FFE0F7FF", "#FFB3E5FC"), 8, true, Optional.empty(), Optional.empty()))
                                .forGetter(VisualSettings::background),
                        Codec.STRING.optionalFieldOf("theme_color")
                                .forGetter(VisualSettings::themeColor),
                        Codec.INT.optionalFieldOf("width", 280)
                                .forGetter(VisualSettings::width),
                        Codec.INT.optionalFieldOf("height", 180)
                                .forGetter(VisualSettings::height),
                        Position.CODEC.optionalFieldOf("position", new Position("BOTTOM_CENTER", 0, 0, false))
                                .forGetter(VisualSettings::position),
                        Codec.FLOAT.optionalFieldOf("animation_speed", 1.0f)
                                .forGetter(VisualSettings::animationSpeed),
                        Position.CODEC.optionalFieldOf("animation_from")
                                .forGetter(VisualSettings::animationFrom),
                        Position.CODEC.optionalFieldOf("animation_to")
                                .forGetter(VisualSettings::animationTo),
                        ResourceLocation.CODEC.optionalFieldOf("hover_animation_style", new ResourceLocation(AuraTip.MOD_ID, "none"))
                                .forGetter(VisualSettings::hoverAnimationStyle),
                        Codec.FLOAT.optionalFieldOf("hover_animation_speed", 1.0f)
                                .forGetter(VisualSettings::hoverAnimationSpeed),
                        Codec.BOOL.optionalFieldOf("hover_only_on_hover", false)
                                .forGetter(VisualSettings::hoverOnlyOnHover),
                        Codec.INT.optionalFieldOf("stripe_width", 4)
                                .forGetter(VisualSettings::stripeWidth),
                        Codec.FLOAT.optionalFieldOf("stripe_length_factor", 1.0f)
                                .forGetter(VisualSettings::stripeLengthFactor),
                        AnimationParams.CODEC.optionalFieldOf("animation_params", AnimationParams.EMPTY)
                                .forGetter(VisualSettings::animationParams),
                        LayoutConfig.CODEC.optionalFieldOf("layout", LayoutConfig.DEFAULT)
                                .forGetter(VisualSettings::layout)
                ).apply(inst, VisualSettings::new)
        );

        public enum BackgroundType {
            GRADIENT,
            SOLID,
            IMAGE;

            public static final Codec<BackgroundType> CODEC = CodecUtil.enumCodec(BackgroundType.class);
        }

        public record Background(
                BackgroundType type,
                List<String> colors,
                int borderRadius,
                boolean rounded,
                Optional<String> imagePath,
                Optional<ShadowConfig> shadow
        ) {
            public static final Codec<Background> CODEC = RecordCodecBuilder.create(inst ->
                    inst.group(
                            BackgroundType.CODEC.optionalFieldOf("type", BackgroundType.GRADIENT).forGetter(Background::type),
                            Codec.STRING.listOf().optionalFieldOf("colors", List.of("#FFE0F7FF", "#FFB3E5FC")).forGetter(Background::colors),
                            Codec.INT.optionalFieldOf("border_radius", 8).forGetter(Background::borderRadius),
                            Codec.BOOL.optionalFieldOf("rounded", true).forGetter(Background::rounded),
                            Codec.STRING.optionalFieldOf("image_path").forGetter(Background::imagePath),
                            ShadowConfig.CODEC.optionalFieldOf("shadow").forGetter(Background::shadow)
                    ).apply(inst, Background::new)
            );
        }

        public record ShadowConfig(boolean enabled, int color, int offsetX, int offsetY, int size) {
            public static final Codec<ShadowConfig> CODEC = RecordCodecBuilder.create(inst ->
                    inst.group(
                            Codec.BOOL.optionalFieldOf("enabled", false).forGetter(ShadowConfig::enabled),
                            CodecUtil.argbOrInt().optionalFieldOf("color", 0x8C000000).forGetter(ShadowConfig::color),
                            Codec.INT.optionalFieldOf("offset_x", 2).forGetter(ShadowConfig::offsetX),
                            Codec.INT.optionalFieldOf("offset_y", 2).forGetter(ShadowConfig::offsetY),
                            Codec.INT.optionalFieldOf("size", 4).forGetter(ShadowConfig::size)
                    ).apply(inst, ShadowConfig::new)
            );
        }
    }

    public record Behavior(
            int defaultDuration,
            boolean pauseTimerOnHover,
            Optional<String> closableByKey,
            boolean allowPaging,
            boolean showCloseButton,
            boolean showPageIndicator
    ) {
        public static final Codec<Behavior> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.INT.optionalFieldOf("default_duration", 200)
                                .forGetter(Behavior::defaultDuration),
                        Codec.BOOL.optionalFieldOf("pause_timer_on_hover", true)
                                .forGetter(Behavior::pauseTimerOnHover),
                        Codec.STRING.optionalFieldOf("closable_by_key")
                                .forGetter(Behavior::closableByKey),
                        Codec.BOOL.optionalFieldOf("allow_paging", true)
                                .forGetter(Behavior::allowPaging),
                        Codec.BOOL.optionalFieldOf("show_close_button", true)
                                .forGetter(Behavior::showCloseButton),
                        Codec.BOOL.optionalFieldOf("show_page_indicator", true)
                                .forGetter(Behavior::showPageIndicator)
                ).apply(inst, Behavior::new)
        );
    }

    public record Page(
            int pageIndex,
            Optional<ComponentSerialization.TextElement> title,
            Optional<ComponentSerialization.TextElement> subtitle,
            Optional<ComponentSerialization.TextElement> content,
            Optional<ImageElement> image,
            Optional<Badge> badge
    ) {
        public static final Codec<Page> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.INT.fieldOf("page_index").forGetter(Page::pageIndex),
                        ComponentSerialization.TextElement.CODEC.optionalFieldOf("title").forGetter(Page::title),
                        ComponentSerialization.TextElement.CODEC.optionalFieldOf("subtitle").forGetter(Page::subtitle),
                        ComponentSerialization.TextElement.CODEC.optionalFieldOf("content").forGetter(Page::content),
                        ImageElement.CODEC.optionalFieldOf("image").forGetter(Page::image),
                        Badge.CODEC.optionalFieldOf("badge").forGetter(Page::badge)
                ).apply(inst, Page::new)
        );
    }

    public record Badge(
            ComponentSerialization.TextElement text,
            int backgroundColor,
            int radius,
            Position position
    ) {
        public static final Codec<Badge> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        ComponentSerialization.TextElement.CODEC.fieldOf("text").forGetter(Badge::text),
                        CodecUtil.argbOrInt().optionalFieldOf("background_color", 0xCC000000).forGetter(Badge::backgroundColor),
                        Codec.INT.optionalFieldOf("radius", 4).forGetter(Badge::radius),
                        Position.CODEC.optionalFieldOf("position", new Position("BOTTOM_RIGHT", 0, 0, false)).forGetter(Badge::position)
                ).apply(inst, Badge::new)
        );
    }

    public record ImageElement(String path, Position position, int[] size, float scale) {
        public static final Codec<ImageElement> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.STRING.fieldOf("path").forGetter(ImageElement::path),
                        Position.CODEC.optionalFieldOf("position", new Position("TOP_CENTER", 0, 0, false)).forGetter(ImageElement::position),
                        Codec.INT.listOf()
                                .optionalFieldOf("size", List.of(64, 64))
                                .xmap(
                                        list -> list.stream().mapToInt(Integer::intValue).toArray(),
                                        arr -> List.of(arr[0], arr[1])
                                )
                                .forGetter(ImageElement::size),
                        Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(ImageElement::scale)
                ).apply(inst, ImageElement::new)
        );

        public ImageElement {
            if (size == null || size.length != 2) {
                size = new int[]{64, 64};
            }
            if (scale <= 0.0f) {
                scale = 1.0f;
            }
        }
    }
}
