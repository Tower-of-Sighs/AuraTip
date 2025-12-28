package cc.sighs.auratip.compat.kubejs.radiamenu;

import cc.sighs.auratip.data.RadialMenuData;
import cc.sighs.auratip.data.action.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RadialMenuBuilder {

    private final String id;
    private final List<RadialMenuData.Slot> slots = new ArrayList<>();
    private int innerRadius = 40;
    private int outerRadius = 80;
    private float animationSpeed = 1.0f;
    private ResourceLocation centerIcon;
    private String ringColor;
    private List<String> ringColors = new ArrayList<>();

    public RadialMenuBuilder(String id) {
        this.id = id;
    }

    public RadialMenuBuilder radii(int inner, int outer) {
        this.innerRadius = inner;
        this.outerRadius = outer;
        return this;
    }

    public RadialMenuBuilder animationSpeed(float speed) {
        this.animationSpeed = speed;
        return this;
    }

    public RadialMenuBuilder centerIcon(String id) {
        if (id == null || id.isEmpty()) {
            this.centerIcon = null;
        } else {
            this.centerIcon = new ResourceLocation(id);
        }
        return this;
    }

    public RadialMenuBuilder ringColor(String color) {
        this.ringColor = color;
        return this;
    }

    public RadialMenuBuilder ringColors(List<String> colors) {
        this.ringColors = colors == null ? new ArrayList<>() : new ArrayList<>(colors);
        return this;
    }

    public RadialMenuBuilder slot(String name, String iconId, Action action, Component text, String highlightColor) {
        if (name == null || name.isEmpty() || iconId == null || iconId.isEmpty() || action == null) {
            return this;
        }
        ResourceLocation icon = new ResourceLocation(iconId);
        RadialMenuData.Slot slot = new RadialMenuData.Slot(
                name,
                icon,
                action,
                Optional.ofNullable(text),
                Optional.ofNullable(highlightColor)
        );
        this.slots.add(slot);
        return this;
    }

    RadialMenuData build() {
        RadialMenuData.MenuSettings settings = new RadialMenuData.MenuSettings(
                innerRadius,
                outerRadius,
                animationSpeed,
                Optional.ofNullable(centerIcon),
                Optional.ofNullable(ringColor),
                ringColors == null || ringColors.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(ringColors))
        );
        return new RadialMenuData(settings, List.copyOf(slots));
    }
}