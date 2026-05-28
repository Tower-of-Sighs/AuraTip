package cc.sighs.auratip.api.radiamenu.icon;

import cc.sighs.auratip.AuraTip;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;

/**
 * Renders a texture (like a sprite or PNG) as a radial menu slot icon.
 *
 * @param id texture path (e.g. {@code minecraft:textures/item/apple.png})
 */
public record TextureIcon(ResourceLocation id, float scale) implements IRadialIcon {

    public static final ResourceLocation TYPE = new ResourceLocation(AuraTip.MOD_ID, "texture");

    public static final Codec<TextureIcon> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    ResourceLocation.CODEC.fieldOf("id").forGetter(TextureIcon::id),
                    Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(TextureIcon::scale)
            ).apply(inst, TextureIcon::new)
    );

    @Override
    public void render(GuiGraphics graphics, int x, int y, float s, float alpha) {
        float finalScale = s * scale;
        if (finalScale <= 0.0f || alpha <= 0.0f) return;
        int size = (int) (24 * finalScale);
        int drawX = x - size / 2;
        int drawY = y - size / 2;

        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, id);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
        graphics.blit(id, drawX, drawY, 0, 0, size, size, size, size);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
    }

    @Override
    public Codec<TextureIcon> codec() {
        return CODEC;
    }
}
