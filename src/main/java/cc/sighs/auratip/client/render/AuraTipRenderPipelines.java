package cc.sighs.auratip.client.render;

import cc.sighs.auratip.AuraTip;
import com.mojang.blaze3d.pipeline.BlendFunction;
import com.mojang.blaze3d.pipeline.ColorTargetState;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

public final class AuraTipRenderPipelines {

    public static final VertexFormat RADIAL_RING_VERTEX_FORMAT = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("Color", VertexFormatElement.COLOR)
            .add("UV0", VertexFormatElement.UV0)
            .add("UV1", VertexFormatElement.UV1)
            .add("UV2", VertexFormatElement.UV2)
            .add("Normal", VertexFormatElement.NORMAL)
            .padding(1)
            .add("LineWidth", VertexFormatElement.LINE_WIDTH)
            .build();

    public static RenderPipeline RADIAL_RING;
    public static RenderPipeline ROUNDED_PANEL;

    private AuraTipRenderPipelines() {
    }

    public static void onRegisterRenderPipelines(RegisterRenderPipelinesEvent event) {
        RADIAL_RING = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(AuraTip.MOD_ID, "pipeline/radial_ring"))
                .withVertexShader(Identifier.fromNamespaceAndPath(AuraTip.MOD_ID, "core/radial_uv"))
                .withFragmentShader(Identifier.fromNamespaceAndPath(AuraTip.MOD_ID, "core/radial_ring"))
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withCull(false)
                .withVertexFormat(RADIAL_RING_VERTEX_FORMAT, VertexFormat.Mode.QUADS)
                .build();

        event.registerPipeline(RADIAL_RING);

        ROUNDED_PANEL = RenderPipeline.builder(RenderPipelines.MATRICES_PROJECTION_SNIPPET)
                .withLocation(Identifier.fromNamespaceAndPath(AuraTip.MOD_ID, "pipeline/rounded_panel"))
                .withVertexShader("core/gui")
                .withFragmentShader("core/gui")
                .withColorTargetState(new ColorTargetState(BlendFunction.TRANSLUCENT))
                .withCull(false)
                .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
                .build();

        event.registerPipeline(ROUNDED_PANEL);
    }
}
