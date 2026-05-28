package cc.sighs.auratip.api.radiamenu.icon;

import cc.sighs.auratip.AuraTip;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;

/**
 * Renders a texture (like a sprite or PNG) as a radial menu slot icon.
 *
 * @param id texture path (e.g. {@code minecraft:textures/item/apple.png})
 * @param scale    render scale multiplier (default 1.0)
 */
public record TextureIcon(Identifier id, float scale) implements IRadialIcon {

    public static final Identifier TYPE = AuraTip.id("texture");

    public static final Codec<TextureIcon> CODEC = RecordCodecBuilder.create(inst ->
            inst.group(
                    Identifier.CODEC.fieldOf("id").forGetter(TextureIcon::id),
                    Codec.FLOAT.optionalFieldOf("scale", 1.0f).forGetter(TextureIcon::scale)
            ).apply(inst, TextureIcon::new)
    );

    static {
        IRadialIcon.register(TYPE, TextureIcon.class, CODEC);
    }

    @Override
    public void render(GuiGraphicsExtractor graphics, int x, int y, float s, float alpha) {
        float finalScale = s * scale;
        if (finalScale <= 0.0f || alpha <= 0.0f) {
            return;
        }
        int size = (int) (24 * finalScale);
        int drawX = x - size / 2;
        int drawY = y - size / 2;
        int color = ARGB.white(alpha);

        graphics.blit(RenderPipelines.GUI_TEXTURED, id, drawX, drawY, 0.0f, 0.0f, size, size, size, size, color);
    }

    @Override
    public Codec<TextureIcon> codec() {
        return CODEC;
    }
}
