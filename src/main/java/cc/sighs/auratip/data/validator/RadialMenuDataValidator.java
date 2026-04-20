package cc.sighs.auratip.data.validator;

import cc.sighs.auratip.data.RadialMenuData;
import cc.sighs.oelib.data.api.DataValidator;
import net.minecraft.resources.Identifier;

public class RadialMenuDataValidator implements DataValidator<RadialMenuData> {
    @Override
    public ValidationResult validate(RadialMenuData data, Identifier source) {
        if (data.id() == null) {
            return ValidationResult.failure("Radial menu has no id in " + source);
        }
        if (data.menuSettings() == null) {
            return ValidationResult.failure("Radial menu has no menu_settings in " + source);
        }

        var settings = data.menuSettings();
        if (settings.innerRadius() <= 0 || settings.outerRadius() <= 0) {
            return ValidationResult.failure("inner_radius and outer_radius must be > 0 in " + source);
        }
        if (settings.innerRadius() >= settings.outerRadius()) {
            return ValidationResult.failure("inner_radius must be < outer_radius in " + source);
        }
        if (settings.animationSpeed() <= 0) {
            return ValidationResult.failure("animation_speed must be > 0 in " + source);
        }

        var slots = data.slots();
        if (slots == null || slots.isEmpty()) {
            return ValidationResult.failure("Radial menu has no slots in " + source);
        }

        for (int i = 0; i < slots.size(); i++) {
            var slot = slots.get(i);
            if (slot.name() == null || slot.name().isBlank()) {
                return ValidationResult.failure("Slot " + i + " has empty name in " + source);
            }
            if (slot.icon() == null) {
                return ValidationResult.failure("Slot " + i + " has no icon in " + source);
            }
            if (slot.action() == null) {
                return ValidationResult.failure("Slot " + i + " has no action in " + source);
            }
        }

        return ValidationResult.success();
    }
}
