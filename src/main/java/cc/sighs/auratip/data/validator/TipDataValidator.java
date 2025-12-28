package cc.sighs.auratip.data.validator;

import cc.sighs.auratip.data.TipData;
import com.mafuyu404.oelib.api.data.DataValidator;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class TipDataValidator implements DataValidator<TipData> {
    @Override
    public ValidationResult validate(TipData data, ResourceLocation source) {
        if (data.id() == null || data.id().isBlank()) {
            return ValidationResult.failure("Tip id is empty in " + source);
        }

        var pages = data.pages();
        if (pages == null || pages.isEmpty()) {
            return ValidationResult.failure("Tip " + data.id() + " has no pages in " + source);
        }

        Set<Integer> indices = new HashSet<>();
        for (TipData.Page page : pages) {
            if (!indices.add(page.pageIndex())) {
                return ValidationResult.failure("Duplicate page_index " + page.pageIndex() + " in tip " + data.id() + " at " + source);
            }
            boolean hasContent = page.title().isPresent()
                    || page.subtitle().isPresent()
                    || page.content().isPresent()
                    || page.image().isPresent();
            if (!hasContent) {
                return ValidationResult.failure("Page " + page.pageIndex() + " of tip " + data.id() + " has no content in " + source);
            }
        }

        var visual = data.visualSettings();
        if (visual == null) {
            return ValidationResult.failure("visual_settings is missing in tip " + data.id() + " at " + source);
        }
        if (visual.width() <= 0 || visual.height() <= 0) {
            return ValidationResult.failure("width and height must be > 0 in tip " + data.id() + " at " + source);
        }
        var background = visual.background();
        if (background != null) {
            if (background.borderRadius() < 0) {
                return ValidationResult.failure("background.border_radius must be >= 0 in tip " + data.id() + " at " + source);
            }
            if (background.type() == TipData.VisualSettings.BackgroundType.GRADIENT
                    || background.type() == TipData.VisualSettings.BackgroundType.SOLID) {
                var colors = background.colors();
                if (colors == null || colors.isEmpty()) {
                    return ValidationResult.failure(background.type().name().toLowerCase(Locale.ROOT) + " background requires at least one color in tip " + data.id() + " at " + source);
                }
            }
        }

        var pos = visual.position();
        if (pos != null && pos.absolute()) {
            if (pos.x() < 0 || pos.y() < 0) {
                return ValidationResult.failure("visual position coordinates must be >= 0 in tip " + data.id() + " at " + source);
            }
        }

        var animationFrom = visual.animationFrom();
        if (animationFrom.isPresent() && animationFrom.get().absolute()) {
            var p = animationFrom.get();
            if (p.x() < 0 || p.y() < 0) {
                return ValidationResult.failure("animation_from coordinates must be >= 0 in tip " + data.id() + " at " + source);
            }
        }

        var animationTo = visual.animationTo();
        if (animationTo.isPresent() && animationTo.get().absolute()) {
            var p = animationTo.get();
            if (p.x() < 0 || p.y() < 0) {
                return ValidationResult.failure("animation_to coordinates must be >= 0 in tip " + data.id() + " at " + source);
            }
        }

        if (visual.hoverAnimationSpeed() < 0.0f) {
            return ValidationResult.failure("hover_animation_speed must be >= 0 in tip " + data.id() + " at " + source);
        }

        var behavior = data.behavior();
        if (behavior.defaultDuration() < -1) {
            return ValidationResult.failure("default_duration must be >= -1 in tip " + data.id() + " at " + source);
        }

        if (behavior.closableByKey().isPresent() && behavior.closableByKey().get().isBlank()) {
            return ValidationResult.failure("closable_by_key is blank in tip " + data.id() + " at " + source);
        }

        return ValidationResult.success();
    }
}
