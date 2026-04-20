package cc.sighs.auratip.data;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.data.action.Action;
import cc.sighs.auratip.data.validator.RadialMenuDataValidator;
import cc.sighs.oelib.data.api.DataDriven;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

@DataDriven(
        modid = AuraTip.MOD_ID,
        folder = "radial_menu",
        syncToClient = true,
        supportArray = true,
        validator = RadialMenuDataValidator.class
)
public record RadialMenuData(
        Identifier id,
        MenuSettings menuSettings,
        List<Slot> slots
) {
    public static final Codec<RadialMenuData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("id").forGetter(RadialMenuData::id),
                    MenuSettings.CODEC.fieldOf("menu_settings").forGetter(RadialMenuData::menuSettings),
                    Slot.CODEC.listOf().fieldOf("slots").forGetter(RadialMenuData::slots)
            ).apply(instance, RadialMenuData::new)
    );

    public record Slot(
            String name,
            Identifier icon,
            Action action,
            Optional<Component> text,
            Optional<String> highlightColor
    ) {
        public static final Codec<Slot> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.STRING.fieldOf("name").forGetter(Slot::name),
                        Identifier.CODEC.fieldOf("icon").forGetter(Slot::icon),
                        Action.CODEC.fieldOf("action").forGetter(Slot::action),
                        ComponentSerialization.CODEC.optionalFieldOf("text").forGetter(Slot::text),
                        Codec.STRING.optionalFieldOf("highlight_color").forGetter(Slot::highlightColor)
                ).apply(inst, Slot::new)
        );
    }

    public record MenuSettings(
            int innerRadius,
            int outerRadius,
            float animationSpeed,
            Optional<Identifier> centerIcon,
            Optional<String> ringColor,
            Optional<List<String>> ringColors
    ) {
        public static final Codec<MenuSettings> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.INT.fieldOf("inner_radius").forGetter(MenuSettings::innerRadius),
                        Codec.INT.fieldOf("outer_radius").forGetter(MenuSettings::outerRadius),
                        Codec.FLOAT.fieldOf("animation_speed").forGetter(MenuSettings::animationSpeed),
                        Identifier.CODEC.optionalFieldOf("center_icon").forGetter(MenuSettings::centerIcon),
                        Codec.STRING.optionalFieldOf("ring_color").forGetter(MenuSettings::ringColor),
                        Codec.list(Codec.STRING).optionalFieldOf("ring_colors").forGetter(MenuSettings::ringColors)
                ).apply(inst, MenuSettings::new)
        );
    }
}
