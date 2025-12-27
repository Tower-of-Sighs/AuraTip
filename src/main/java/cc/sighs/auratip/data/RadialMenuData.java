package cc.sighs.auratip.data;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.data.action.Action;
import cc.sighs.auratip.data.validator.RadialMenuDataValidator;
import cc.sighs.auratip.util.ComponentSerialization;
import com.mafuyu404.oelib.api.data.DataDriven;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Optional;

@DataDriven(
        modid = AuraTip.MODID,
        folder = "radial_memu",
        syncToClient = true,
        supportArray = true,
        validator = RadialMenuDataValidator.class
)
public record RadialMenuData(
        MenuSettings menuSettings,
        List<Slot> slots
) {
    public static final Codec<RadialMenuData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    MenuSettings.CODEC.fieldOf("menu_settings").forGetter(RadialMenuData::menuSettings),
                    Slot.CODEC.listOf().fieldOf("slots").forGetter(RadialMenuData::slots)
            ).apply(instance, RadialMenuData::new)
    );

    public record Slot(
            String name,
            ResourceLocation icon,
            Action action,
            Optional<Component> text,
            Optional<String> highlightColor
    ) {
        public static final Codec<Slot> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.STRING.fieldOf("name").forGetter(Slot::name),
                        ResourceLocation.CODEC.fieldOf("icon").forGetter(Slot::icon),
                        Action.CODEC.fieldOf("action").forGetter(Slot::action),
                        ComponentSerialization.COMPONENT_CODEC.optionalFieldOf("text").forGetter(Slot::text),
                        Codec.STRING.optionalFieldOf("highlight_color").forGetter(Slot::highlightColor)
                ).apply(inst, Slot::new)
        );
    }

    public record MenuSettings(
            int innerRadius,
            int outerRadius,
            float animationSpeed,
            Optional<ResourceLocation> centerIcon,
            Optional<String> ringColor,
            Optional<List<String>> ringColors
    ) {
        public static final Codec<MenuSettings> CODEC = RecordCodecBuilder.create(inst ->
                inst.group(
                        Codec.INT.fieldOf("inner_radius").forGetter(MenuSettings::innerRadius),
                        Codec.INT.fieldOf("outer_radius").forGetter(MenuSettings::outerRadius),
                        Codec.FLOAT.fieldOf("animation_speed").forGetter(MenuSettings::animationSpeed),
                        ResourceLocation.CODEC.optionalFieldOf("center_icon").forGetter(MenuSettings::centerIcon),
                        Codec.STRING.optionalFieldOf("ring_color").forGetter(MenuSettings::ringColor),
                        Codec.list(Codec.STRING).optionalFieldOf("ring_colors").forGetter(MenuSettings::ringColors)
                ).apply(inst, MenuSettings::new)
        );
    }
}
