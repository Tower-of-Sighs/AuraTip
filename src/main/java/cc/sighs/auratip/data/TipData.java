package cc.sighs.auratip.data;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.data.validator.TipDataValidator;
import cc.sighs.auratip.util.ComponentSerialization;
import com.mafuyu404.oelib.api.data.DataDriven;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@DataDriven(
        modid = AuraTip.MODID,
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
            boolean blurBackground,
            String themeColor,
            int width,
            int height
    ) {
        public static final Codec<VisualSettings> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.STRING.optionalFieldOf("animation_style", "fade_and_slide")
                                .forGetter(VisualSettings::animationStyle),
                        Codec.BOOL.optionalFieldOf("blur_background", true)
                                .forGetter(VisualSettings::blurBackground),
                        Codec.STRING.optionalFieldOf("theme_color", "#A0D8EF")
                                .forGetter(VisualSettings::themeColor),
                        Codec.INT.optionalFieldOf("width", 280)
                                .forGetter(VisualSettings::width),
                        Codec.INT.optionalFieldOf("height", 180)
                                .forGetter(VisualSettings::height)
                ).apply(inst, VisualSettings::new)
        );
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
            Optional<TextElement> title,
            Optional<TextElement> subtitle,
            Optional<TextElement> content,
            Optional<ImageElement> image
    ) {
        public static final Codec<Page> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.INT.fieldOf("page_index").forGetter(Page::pageIndex),
                        TextElement.CODEC.optionalFieldOf("title").forGetter(Page::title),
                        TextElement.CODEC.optionalFieldOf("subtitle").forGetter(Page::subtitle),
                        TextElement.CODEC.optionalFieldOf("content").forGetter(Page::content),
                        ImageElement.CODEC.optionalFieldOf("image").forGetter(Page::image)
                ).apply(inst, Page::new)
        );
    }

    public record TextElement(Component text, float scale, int lineSpacing) {
        public TextElement {
            if (scale <= 0) {
                scale = 1.0f;
            }
            if (lineSpacing < 0) {
                lineSpacing = 0;
            }
        }

        public static final Codec<TextElement> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        ComponentSerialization.COMPONENT_CODEC.fieldOf("text").forGetter(TextElement::text),
                        Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(TextElement::scale),
                        Codec.INT.optionalFieldOf("line_spacing", 0).forGetter(TextElement::lineSpacing)
                ).apply(inst, TextElement::new)
        );
    }

    public record ImageElement(String path, String position, int[] size) {
        public ImageElement {
            if (size == null || size.length != 2) {
                size = new int[]{64, 64};
            }
        }

        public static final Codec<ImageElement> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.STRING.fieldOf("path").forGetter(ImageElement::path),
                        Codec.STRING.optionalFieldOf("position", "TOP_CENTER").forGetter(ImageElement::position),
                        Codec.INT.listOf()
                                .optionalFieldOf("size", List.of(64, 64))
                                .xmap(
                                        list -> list.stream().mapToInt(Integer::intValue).toArray(),
                                        arr -> List.of(arr[0], arr[1])
                                )
                                .forGetter(ImageElement::size)
                ).apply(inst, ImageElement::new)
        );
    }
}
