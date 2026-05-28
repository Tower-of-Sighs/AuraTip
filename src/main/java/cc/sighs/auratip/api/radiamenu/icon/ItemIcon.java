package cc.sighs.auratip.api.radiamenu.icon;

import cc.sighs.auratip.AuraTip;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * Renders an {@link ItemStack} (with its full model) as a radial menu slot icon.
 *
 * @param stack item stack (id + count + components)
 * @param scale render scale multiplier (default 1.0)
 */
public record ItemIcon(ItemStack stack, float scale) implements IRadialIcon {

    public static final ResourceLocation TYPE = AuraTip.id("item");

    public static final Codec<ItemIcon> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    ItemStack.CODEC.fieldOf("stack").forGetter(ItemIcon::stack),
                    Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(ItemIcon::scale)
            ).apply(inst, ItemIcon::new)
    );

    @Override
    public void render(GuiGraphics graphics, int x, int y, float s, float alpha) {
        float finalScale = s * scale;
        if (finalScale <= 0.0f || alpha <= 0.0f) return;

        if (stack.isEmpty()) return;

        int iconSize = (int) (16 * finalScale);
        int drawX = x - iconSize / 2;
        int drawY = y - iconSize / 2;

        graphics.pose().pushPose();
        graphics.pose().translate(drawX, drawY, 0);
        graphics.pose().scale(finalScale, finalScale, 1.0f);
        graphics.setColor(1.0f, 1.0f, 1.0f, alpha);
        graphics.renderFakeItem(stack, 0, 0);
        graphics.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        graphics.pose().popPose();
    }

    @Override
    public Codec<ItemIcon> codec() {
        return CODEC;
    }
}
