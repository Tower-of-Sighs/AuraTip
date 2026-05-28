package cc.sighs.auratip.api.radiamenu.icon;

import cc.sighs.auratip.AuraTip;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

/**
 * Renders an {@link ItemStack} (with its full model) as a radial menu slot icon.
 *
 * @param template item template template (id + count + components)
 * @param scale render scale multiplier (default 1.0)
 */
public record ItemIcon(ItemStackTemplate template, float scale) implements IRadialIcon {

    public static final Identifier TYPE = AuraTip.id("item");

    public static final Codec<ItemIcon> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    ItemStackTemplate.CODEC.fieldOf("template").forGetter(ItemIcon::template),
                    Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(ItemIcon::scale)
            ).apply(inst, ItemIcon::new)
    );

    static {
        IRadialIcon.register(TYPE, ItemIcon.class, CODEC);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int x, int y, float s, float alpha) {
        float finalScale = s * scale;
        if (finalScale <= 0.0f || alpha <= 0.0f) return;
        ItemStack item = template.create();
        if (item.isEmpty()) return;

        int iconSize = (int) (16 * finalScale);
        int drawX = x - iconSize / 2;
        int drawY = y - iconSize / 2;

        graphics.pose().pushMatrix();
        graphics.pose().translate(drawX, drawY);
        graphics.pose().scale(finalScale, finalScale);
        graphics.fakeItem(item, 0, 0);
        graphics.pose().popMatrix();
    }

    @Override
    public Codec<ItemIcon> codec() {
        return CODEC;
    }
}
