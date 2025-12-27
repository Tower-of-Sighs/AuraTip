package cc.sighs.auratip.data.validator;

import cc.sighs.auratip.data.TipData;
import com.mafuyu404.oelib.api.data.DataValidator;
import net.minecraft.resources.ResourceLocation;

import java.util.HashSet;
import java.util.List;
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

