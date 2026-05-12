package cc.sighs.auratip.api.radiamenu.icon;

import cc.sighs.auratip.AuraTip;
import com.mojang.serialization.Codec;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/**
 * Renders a texture (like a sprite or PNG) as a radial menu slot icon.
 *
 * @param location texture path (e.g. {@code minecraft:textures/item/apple.png})
 */
public record TextureIcon(Identifier location) implements IRadialIcon {

    public static final Identifier TYPE = AuraTip.id("texture");

    public static final Codec<TextureIcon> CODEC = Identifier.CODEC
            .xmap(TextureIcon::new, TextureIcon::location)
            .fieldOf("location")
            .codec();

    @Override
    public void render(GuiGraphicsExtractor graphics, int x, int y, float scale, float alpha) {
        if (scale <= 0.0f || alpha <= 0.0f) {
            return;
        }
        int size = (int) (24 * scale);
        int drawX = x - size / 2;
        int drawY = y - size / 2;
        int color = ARGB.white(alpha);

        graphics.blit(RenderPipelines.GUI_TEXTURED, location, drawX, drawY, 0.0f, 0.0f, size, size, size, size,
                color);
    }

    @Override
    public Codec<TextureIcon> codec() {
        return CODEC;
    }
}
