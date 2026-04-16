package cc.sighs.auratip.compat.kubejs.tip;

import cc.sighs.auratip.api.tip.TipBuilder.PageBuilder;
import cc.sighs.auratip.api.tip.TipBuilder.BehaviorBuilder;
import cc.sighs.auratip.data.TipData;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * KubeJS-facing tip builder.
 * <p>
 * This wraps the Java API builder, but keeps the KubeJS method names and string-based ids.
 */
public final class TipBuilder {

    private final cc.sighs.auratip.api.tip.TipBuilder delegate;

    public TipBuilder(String id) {
        this.delegate = new cc.sighs.auratip.api.tip.TipBuilder(normalizeId(id));
    }

    public TipBuilder trigger(String type, String mode, int cooldownTicks) {
        ResourceLocation typeId = normalizeType(type);
        TipData.Trigger.Mode parsed = TipData.Trigger.Mode.ONCE;
        if (mode != null && !mode.isEmpty()) {
            parsed = TipData.Trigger.Mode.valueOf(mode.toUpperCase(Locale.ROOT));
        }
        delegate.trigger(typeId, parsed, cooldownTicks);
        return this;
    }

    public TipBuilder visual(Consumer<VisualBuilder> visual) {
        if (visual == null) {
            return this;
        }
        delegate.visual(v -> visual.accept(new VisualBuilder(v)));
        return this;
    }

    public TipBuilder behavior(Consumer<BehaviorBuilder> behavior) {
        delegate.behavior(behavior);
        return this;
    }

    public TipBuilder page(int index, Consumer<PageBuilder> page) {
        delegate.page(index, page);
        return this;
    }

    public TipData build() {
        return delegate.build();
    }

    public static final class VisualBuilder {
        private final cc.sighs.auratip.api.tip.TipBuilder.VisualBuilder delegate;

        private VisualBuilder(cc.sighs.auratip.api.tip.TipBuilder.VisualBuilder delegate) {
            this.delegate = Objects.requireNonNull(delegate, "delegate");
        }

        public VisualBuilder animationStyle(String id) {
            delegate.animationStyle(normalizeAnimation(id));
            return this;
        }

        public VisualBuilder animationSpeed(float speed) {
            delegate.animationSpeed(speed);
            return this;
        }

        public VisualBuilder hoverAnimationStyle(String id) {
            delegate.hoverAnimationStyle(normalizeAnimation(id));
            return this;
        }

        public VisualBuilder hoverAnimationSpeed(float speed) {
            delegate.hoverAnimationSpeed(speed);
            return this;
        }

        public VisualBuilder hoverOnlyOnHover(boolean value) {
            delegate.hoverOnlyOnHover(value);
            return this;
        }

        public VisualBuilder stripeWidth(int width) {
            delegate.stripeWidth(width);
            return this;
        }

        public VisualBuilder stripeLengthFactor(float factor) {
            delegate.stripeLengthFactor(factor);
            return this;
        }

        public VisualBuilder animParam(String key, Object value) {
            delegate.animParam(key, value);
            return this;
        }

        public VisualBuilder animParams(Map<String, ?> params) {
            delegate.animParams(params);
            return this;
        }

        public VisualBuilder hoverParam(String key, Object value) {
            delegate.hoverParam(key, value);
            return this;
        }

        public VisualBuilder hoverParams(Map<String, ?> params) {
            delegate.hoverParams(params);
            return this;
        }

        public VisualBuilder themeColor(String argbHex) {
            delegate.themeColor(argbHex);
            return this;
        }

        public VisualBuilder size(int w, int h) {
            delegate.size(w, h);
            return this;
        }

        public VisualBuilder position(String preset) {
            delegate.positionPreset(preset);
            return this;
        }

        public VisualBuilder position(int x, int y) {
            delegate.positionAbsolute(x, y);
            return this;
        }

        public VisualBuilder animationFrom(String preset) {
            delegate.animationFromPreset(preset);
            return this;
        }

        public VisualBuilder animationFrom(int x, int y) {
            delegate.animationFromAbsolute(x, y);
            return this;
        }

        public VisualBuilder animationTo(String preset) {
            delegate.animationToPreset(preset);
            return this;
        }

        public VisualBuilder animationTo(int x, int y) {
            delegate.animationToAbsolute(x, y);
            return this;
        }

        public VisualBuilder background(String type, List<String> colors, int radius) {
            TipData.VisualSettings.BackgroundType parsed = TipData.VisualSettings.BackgroundType.valueOf(type.toUpperCase(Locale.ROOT));
            delegate.background(parsed, colors, radius);
            return this;
        }

        public VisualBuilder backgroundRounded(boolean value) {
            delegate.backgroundRounded(value);
            return this;
        }

        public VisualBuilder backgroundImage(String path) {
            delegate.backgroundImage(path);
            return this;
        }
    }

    private static ResourceLocation normalizeId(String id) {
        if (id == null || id.isEmpty()) {
            return new ResourceLocation("kubejs", "tip");
        }
        if (id.indexOf(':') < 0) {
            return new ResourceLocation("kubejs", id);
        }
        return new ResourceLocation(id);
    }

    private static ResourceLocation normalizeType(String type) {
        String raw = (type == null) ? "" : type.trim();
        if (raw.isEmpty()) {
            return new ResourceLocation("kubejs", "trigger");
        }
        String lower = raw.toLowerCase(Locale.ROOT);
        if (lower.indexOf(':') >= 0) {
            ResourceLocation parsed = ResourceLocation.tryParse(lower);
            if (parsed == null) {
                throw new IllegalStateException("Invalid trigger type id: " + raw);
            }
            return parsed;
        }
        return new ResourceLocation("kubejs", lower);
    }

    private static ResourceLocation normalizeAnimation(String id) {
        String raw = (id == null) ? "" : id.trim();
        if (raw.isEmpty()) {
            return new ResourceLocation("kubejs", "animation");
        }
        String lower = raw.toLowerCase(Locale.ROOT);
        if (lower.indexOf(':') >= 0) {
            ResourceLocation parsed = ResourceLocation.tryParse(lower);
            if (parsed == null) {
                throw new IllegalStateException("Invalid animation id: " + raw);
            }
            return parsed;
        }
        return new ResourceLocation("kubejs", lower);
    }
}
