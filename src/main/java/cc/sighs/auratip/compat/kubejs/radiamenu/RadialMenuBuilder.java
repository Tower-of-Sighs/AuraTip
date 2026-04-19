package cc.sighs.auratip.compat.kubejs.radiamenu;

import cc.sighs.auratip.data.RadialMenuData;
import cc.sighs.auratip.data.action.Action;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

/**
 * KubeJS-facing radial menu builder.
 * <p>
 * This wraps the Java API builder, but keeps the KubeJS method names and string-based ids/icons.
 */
public final class RadialMenuBuilder {

    private final cc.sighs.auratip.api.radiamenu.RadialMenuBuilder delegate;

    public RadialMenuBuilder(String id) {
        this.delegate = new cc.sighs.auratip.api.radiamenu.RadialMenuBuilder(normalizeId(id));
    }

    public RadialMenuBuilder radii(int inner, int outer) {
        delegate.radii(inner, outer);
        return this;
    }

    public RadialMenuBuilder animationSpeed(float speed) {
        delegate.animationSpeed(speed);
        return this;
    }

    public RadialMenuBuilder centerIcon(@Nullable String iconId) {
        if (iconId == null || iconId.isEmpty()) {
            delegate.centerIcon(null);
        } else {
            delegate.centerIcon(ResourceLocation.parse(iconId));
        }
        return this;
    }

    public RadialMenuBuilder ringColor(@Nullable String color) {
        delegate.ringColor(color);
        return this;
    }

    public RadialMenuBuilder ringColors(@Nullable List<String> colors) {
        delegate.ringColors(colors);
        return this;
    }

    public RadialMenuBuilder slot(
            String name,
            String iconId,
            Action action,
            @Nullable Component text,
            @Nullable String highlightColor
    ) {
        Objects.requireNonNull(iconId, "iconId");
        delegate.slot(name, ResourceLocation.parse(iconId), action, text, highlightColor);
        return this;
    }

    public RadialMenuData build() {
        return delegate.build();
    }

    private static ResourceLocation normalizeId(String id) {
        if (id == null || id.isEmpty()) {
            return ResourceLocation.fromNamespaceAndPath("kubejs", "radial_menu");
        }
        if (id.indexOf(':') < 0) {
            return ResourceLocation.fromNamespaceAndPath("kubejs", id);
        }
        return ResourceLocation.parse(id);
    }
}
