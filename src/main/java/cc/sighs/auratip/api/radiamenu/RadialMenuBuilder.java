package cc.sighs.auratip.api.radiamenu;

import cc.sighs.auratip.data.RadialMenuData;
import cc.sighs.auratip.data.action.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Objects;

/**
 * Builds {@link RadialMenuData}.
 * <p>
 * Put built menus into {@link RadialMenuRegistry}, or add slots via {@link RadialMenuExtraSlots}.
 */
public class RadialMenuBuilder {

    private final ResourceLocation id;
    private final List<RadialMenuData.Slot> slots = new ArrayList<>();

    private int innerRadius = 40;
    private int outerRadius = 80;
    private float animationSpeed = 1.0f;
    private ResourceLocation centerIcon;
    private String ringColor;
    private List<String> ringColors = new ArrayList<>();

    /**
     * Creates a new radial menu builder.
     *
     * @param id logical id for this menu
     */
    public RadialMenuBuilder(ResourceLocation id) {
        this.id = Objects.requireNonNull(id, "id");
    }

    /**
     * Returns the logical id passed to the constructor.
     */
    public ResourceLocation id() {
        return id;
    }

    /**
     * Sets inner/outer radii (in pixels).
     *
     * @param inner inner radius
     * @param outer outer radius (should be &gt; inner)
     */
    public RadialMenuBuilder radii(int inner, int outer) {
        this.innerRadius = inner;
        this.outerRadius = outer;
        return this;
    }

    /**
     * Sets the open/close animation speed multiplier (&gt; 0 recommended).
     */
    public RadialMenuBuilder animationSpeed(float speed) {
        this.animationSpeed = speed;
        return this;
    }

    /**
     * Sets the center icon.
     *
     * @param icon icon texture location; pass null to clear
     */
    public RadialMenuBuilder centerIcon(@Nullable ResourceLocation icon) {
        this.centerIcon = icon;
        return this;
    }

    /**
     * Sets a single ring color (argb hex).
     * <p>
     * If you also set {@link #ringColors(List)}, the renderer may prefer gradient colors depending on its logic.
     */
    public RadialMenuBuilder ringColor(@Nullable String color) {
        this.ringColor = color;
        return this;
    }

    /**
     * Sets gradient ring colors (argb hex list).
     */
    public RadialMenuBuilder ringColors(@Nullable List<String> colors) {
        this.ringColors = colors == null ? new ArrayList<>() : new ArrayList<>(colors);
        return this;
    }

    /**
     * Adds a slot.
     *
     * @param name           slot name (must be non-empty)
     * @param icon           icon texture location
     * @param action         slot action
     * @param text           optional label text
     * @param highlightColor optional hover highlight color (argb hex)
     */
    public RadialMenuBuilder slot(
            String name,
            ResourceLocation icon,
            Action action,
            @Nullable Component text,
            @Nullable String highlightColor
    ) {
        if (name == null || name.isEmpty() || icon == null || action == null) {
            return this;
        }
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

    /**
     * Builds the final {@link RadialMenuData}.
     *
     * @return built menu data
     */
    public RadialMenuData build() {
        RadialMenuData.MenuSettings settings = new RadialMenuData.MenuSettings(
                innerRadius,
                outerRadius,
                animationSpeed,
                Optional.ofNullable(centerIcon),
                Optional.ofNullable(ringColor),
                ringColors == null || ringColors.isEmpty() ? Optional.empty() : Optional.of(List.copyOf(ringColors))
        );
        return new RadialMenuData(id, settings, List.copyOf(slots));
    }
}
