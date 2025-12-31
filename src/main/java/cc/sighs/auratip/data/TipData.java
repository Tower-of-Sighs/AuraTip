package cc.sighs.auratip.data;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.data.validator.TipDataValidator;
import cc.sighs.auratip.util.ComponentSerialization;
import com.mafuyu404.oelib.api.data.DataDriven;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.List;
import java.util.Locale;
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
        String id,
        Trigger trigger,
        VisualSettings visualSettings,
        Behavior behavior,
        List<Page> pages
) {
    public static final Codec<TipData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.STRING.fieldOf("id").forGetter(TipData::id),
                    Trigger.CODEC.fieldOf("trigger").forGetter(TipData::trigger),
                    VisualSettings.CODEC.fieldOf("visual_settings").forGetter(TipData::visualSettings),
                    Behavior.CODEC.fieldOf("behavior").forGetter(TipData::behavior),
                    Page.CODEC.listOf().fieldOf("pages").forGetter(TipData::pages)
            ).apply(instance, TipData::new)
    );

    public static final StreamCodec<ByteBuf, TipData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public record Position(String preset, int x, int y, boolean absolute) {
        public static final Codec<Position> CODEC = Codec.either(
                Codec.STRING,
                Codec.INT.listOf()
        ).xmap(
                either -> {
                    if (either.left().isPresent()) {
                        String p = either.left().get();
                        return new Position(p, 0, 0, false);
                    }
                    var list = either.right().orElse(List.of());
                    int px = !list.isEmpty() ? list.get(0) : 0;
                    int py = list.size() > 1 ? list.get(1) : 0;
                    return new Position(null, px, py, true);
                },
                position -> {
                    if (!position.absolute && position.preset != null) {
                        return Either.left(position.preset);
                    }
                    return Either.right(List.of(position.x, position.y));
                }
        );
    }

    public record Trigger(String type, Mode mode, int cooldown) {
        public static final Codec<Trigger> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.STRING.fieldOf("type").forGetter(Trigger::type),
                        Mode.CODEC.optionalFieldOf("mode", Mode.ONCE).forGetter(Trigger::mode),
                        Codec.INT.optionalFieldOf("cooldown", 0).forGetter(Trigger::cooldown)
                ).apply(inst, Trigger::new)
        );

        public enum Mode {
            ONCE,
            REPEATABLE;

            public static final Codec<Mode> CODEC = Codec.STRING.xmap(
                    value -> Mode.valueOf(value.toUpperCase(Locale.ROOT)),
                    mode -> mode.name().toLowerCase(Locale.ROOT)
            );
        }
    }

    public record VisualSettings(
            String animationStyle,
            Background background,
            Optional<String> themeColor,
            int width,
            int height,
            Position position,
            float animationSpeed,
            Optional<Position> animationFrom,
            Optional<Position> animationTo,
            String hoverAnimationStyle,
            float hoverAnimationSpeed,
            boolean hoverOnlyOnHover,
            int stripeWidth,
            float stripeLengthFactor,
            Map<String, Dynamic<?>> animationParams,
            Map<String, Dynamic<?>> hoverAnimationParams
    ) {
        public static final Codec<VisualSettings> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.STRING.optionalFieldOf("animation_style", "fade_and_slide")
                                .forGetter(VisualSettings::animationStyle),
                        Background.CODEC.optionalFieldOf("background", new Background(BackgroundType.GRADIENT, List.of("#FFE0F7FF", "#FFB3E5FC"), 8, true, Optional.empty()))
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
                        Codec.STRING.optionalFieldOf("hover_animation_style", "none")
                                .forGetter(VisualSettings::hoverAnimationStyle),
                        Codec.FLOAT.optionalFieldOf("hover_animation_speed", 1.0f)
                                .forGetter(VisualSettings::hoverAnimationSpeed),
                        Codec.BOOL.optionalFieldOf("hover_only_on_hover", false)
                                .forGetter(VisualSettings::hoverOnlyOnHover),
                        Codec.INT.optionalFieldOf("stripe_width", 4)
                                .forGetter(VisualSettings::stripeWidth),
                        Codec.FLOAT.optionalFieldOf("stripe_length_factor", 1.0f)
                                .forGetter(VisualSettings::stripeLengthFactor),
                        Codec.unboundedMap(Codec.STRING, Codec.PASSTHROUGH)
                                .optionalFieldOf("animation_params", Map.of())
                                .forGetter(VisualSettings::animationParams),
                        Codec.unboundedMap(Codec.STRING, Codec.PASSTHROUGH)
                                .optionalFieldOf("hover_animation_params", Map.of())
                                .forGetter(VisualSettings::hoverAnimationParams)
                ).apply(inst, VisualSettings::new)
        );

        public enum BackgroundType {
            GRADIENT,
            SOLID,
            IMAGE;

            public static final Codec<BackgroundType> CODEC = Codec.STRING.xmap(
                    value -> BackgroundType.valueOf(value.toUpperCase(Locale.ROOT)),
                    type -> type.name().toLowerCase(Locale.ROOT)
            );
        }

        public record Background(
                BackgroundType type,
                List<String> colors,
                int borderRadius,
                boolean rounded,
                Optional<String> imagePath
        ) {
            public static final Codec<Background> CODEC = RecordCodecBuilder.create(inst ->
                    inst.group(
                            BackgroundType.CODEC.optionalFieldOf("type", BackgroundType.GRADIENT).forGetter(Background::type),
                            Codec.STRING.listOf().optionalFieldOf("colors", List.of("#FFE0F7FF", "#FFB3E5FC")).forGetter(Background::colors),
                            Codec.INT.optionalFieldOf("border_radius", 8).forGetter(Background::borderRadius),
                            Codec.BOOL.optionalFieldOf("rounded", true).forGetter(Background::rounded),
                            Codec.STRING.optionalFieldOf("image_path").forGetter(Background::imagePath)
                    ).apply(inst, Background::new)
            );
        }
    }

    public record Behavior(
            int defaultDuration,
            boolean pauseTimerOnHover,
            Optional<String> closableByKey,
            boolean allowPaging
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
                                .forGetter(Behavior::allowPaging)
                ).apply(inst, Behavior::new)
        );
    }

    public record Page(
            int pageIndex,
            Optional<ComponentSerialization.TextElement> title,
            Optional<ComponentSerialization.TextElement> subtitle,
            Optional<ComponentSerialization.TextElement> content,
            Optional<ImageElement> image
    ) {
        public static final Codec<Page> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.INT.fieldOf("page_index").forGetter(Page::pageIndex),
                        ComponentSerialization.TextElement.CODEC.optionalFieldOf("title").forGetter(Page::title),
                        ComponentSerialization.TextElement.CODEC.optionalFieldOf("subtitle").forGetter(Page::subtitle),
                        ComponentSerialization.TextElement.CODEC.optionalFieldOf("content").forGetter(Page::content),
                        ImageElement.CODEC.optionalFieldOf("image").forGetter(Page::image)
                ).apply(inst, Page::new)
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
