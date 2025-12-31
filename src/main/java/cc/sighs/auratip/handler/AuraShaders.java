package cc.sighs.auratip.handler;

import cc.sighs.auratip.AuraTip;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import lombok.Getter;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;

@EventBusSubscriber(modid = AuraTip.MOD_ID, value = Dist.CLIENT)
public class AuraShaders {

    @Getter
    private static ShaderInstance radialRing;

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(
                        event.getResourceProvider(),
                        ResourceLocation.fromNamespaceAndPath(AuraTip.MOD_ID, "radial_ring"),
                        DefaultVertexFormat.POSITION_TEX
                ),
                shader -> radialRing = shader
        );

    }
}
