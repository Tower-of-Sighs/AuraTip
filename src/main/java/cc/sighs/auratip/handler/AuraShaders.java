package cc.sighs.auratip.handler;

import cc.sighs.auratip.AuraTip;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod.EventBusSubscriber(modid = AuraTip.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AuraShaders {

    private static ShaderInstance radialRing;

    public static ShaderInstance getRadialRing() {
        return radialRing;
    }

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(new ShaderInstance(
                        event.getResourceProvider(),
                        new ResourceLocation(AuraTip.MODID, "radial_ring"),
                        DefaultVertexFormat.POSITION_TEX),
                shader -> radialRing = shader
        );
    }
}