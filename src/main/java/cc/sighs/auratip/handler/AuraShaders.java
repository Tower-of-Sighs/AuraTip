package cc.sighs.auratip.handler;

import cc.sighs.auratip.AuraTip;
import cc.sighs.oelib.registry.extra.ShaderRegister;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;

public class AuraShaders {

    private static ShaderInstance radialRing;

    public static ShaderInstance getRadialRing() {
        return radialRing;
    }

    public static void register() {
        ShaderRegister.register(
                new ResourceLocation(AuraTip.MODID, "radial_ring"),
                DefaultVertexFormat.POSITION_TEX,
                shader -> radialRing = shader
        );
    }
}
