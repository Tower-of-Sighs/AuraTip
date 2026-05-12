package cc.sighs.auratip.api.radiamenu.icon;

import cc.sighs.auratip.AuraTip;
import com.mojang.serialization.Codec;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;

/**
 * Renders an {@link ItemStack} (with its full model) as a radial menu slot icon.
 *
 * @param template an ItemStackTemplate — decodes safely during datapack loading, converts to ItemStack at render time
 */
public record ItemIcon(ItemStackTemplate template) implements IRadialIcon {

    public static final Identifier TYPE = AuraTip.id("item");

    public ItemIcon(ItemStack stack) {
        this(ItemStackTemplate.fromNonEmptyStack(stack));
    }

    public static final Codec<ItemIcon> CODEC = ItemStackTemplate.CODEC
            .xmap(ItemIcon::new, ItemIcon::template);

    @Override
    public void render(GuiGraphicsExtractor graphics, int x, int y, float scale, float alpha) {
        if (scale <= 0.0f || alpha <= 0.0f) {
            return;
        }
        ItemStack stack = template.create();
        if (stack.isEmpty()) return;

        int iconSize = (int) (16 * scale);
        int drawX = x - iconSize / 2;
        int drawY = y - iconSize / 2;

        graphics.pose().pushMatrix();
        graphics.pose().translate(drawX, drawY);
        graphics.pose().scale(scale, scale);
        graphics.fakeItem(stack, 0, 0);
        graphics.pose().popMatrix();
    }

    @Override
    public Codec<ItemIcon> codec() {
        return CODEC;
    }
}
