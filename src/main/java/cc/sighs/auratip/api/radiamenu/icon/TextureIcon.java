package cc.sighs.auratip.api.radiamenu.icon;

import cc.sighs.auratip.AuraTip;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders a texture (like a sprite or PNG) as a radial menu slot icon.
 *
 * @param location texture path (e.g. {@code minecraft:textures/item/apple.png})
 */
public record TextureIcon(ResourceLocation location) implements IRadialIcon {

    public static final ResourceLocation TYPE = new ResourceLocation(AuraTip.MODID, "texture");

    public static final Codec<TextureIcon> CODEC = ResourceLocation.CODEC
            .xmap(TextureIcon::new, TextureIcon::location)
            .fieldOf("location")
            .codec();

    @Override
    public void render(GuiGraphics graphics, int x, int y, float scale, float alpha) {
        if (scale <= 0.0f || alpha <= 0.0f) {
            return;
        }
        int size = (int) (24 * scale);
        int drawX = x - size / 2;
        int drawY = y - size / 2;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, location);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        graphics.blit(location, drawX, drawY, 0, 0, size, size, size, size);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public Codec<TextureIcon> codec() {
        return CODEC;
    }
}
