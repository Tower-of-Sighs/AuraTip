package cc.sighs.auratip.editor.preview;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.client.render.TipOverlay;
import cc.sighs.auratip.data.TipData;
import cc.sighs.auratip.util.ComponentSerialization;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class EditorPreviewApplier {

    private static final ResourceLocation PREVIEW_ID = new ResourceLocation(AuraTip.MODID, "editor_preview");

    private EditorPreviewApplier() {
    }

    public static void applyDefaultPreview() {
        TipOverlay.INSTANCE.show(defaultTip(), Map.of(
                "player", Component.literal("Player")
        ));
    }

    public static void applyTipJson(JsonElement tipJson) {
        if (tipJson == null) {
            return;
        }

        DataResult<TipData> parsed = TipData.CODEC.parse(JsonOps.INSTANCE, tipJson);
        TipData tip = parsed.resultOrPartial(msg -> AuraTip.LOGGER.warn("Editor tip parse error: {}", msg)).orElse(null);
        if (tip == null) {
            return;
        }

        TipData preview = normalizeForPreview(tip);
        Minecraft.getInstance().execute(() -> TipOverlay.INSTANCE.show(preview, Map.of()));
    }

    public static TipData defaultTip() {
        TipData.Trigger trigger = new TipData.Trigger(
                new ResourceLocation(AuraTip.MODID, "editor"),
                TipData.Trigger.Mode.REPEATABLE,
                0
        );

        TipData.VisualSettings.Background background = new TipData.VisualSettings.Background(
                TipData.VisualSettings.BackgroundType.GRADIENT,
                List.of("#FFE0F7FF", "#FFB3E5FC"),
                8,
                true,
                Optional.empty()
        );

        TipData.VisualSettings visual = new TipData.VisualSettings(
                new ResourceLocation(AuraTip.MODID, "fade_and_slide"),
                background,
                Optional.empty(),
                280,
                180,
                new TipData.Position("CENTER", 0, 0, false),
                1.0f,
                Optional.empty(),
                Optional.empty(),
                new ResourceLocation(AuraTip.MODID, "none"),
                1.0f,
                false,
                4,
                1.0f,
                Map.of(),
                Map.of()
        );

        TipData.Behavior behavior = new TipData.Behavior(
                -1,
                true,
                Optional.empty(),
                true
        );

        TipData.Page page = new TipData.Page(
                0,
                Optional.of(new ComponentSerialization.TextElement(
                        Component.literal("AuraTip Editor Preview"),
                        0.85f,
                        0,
                        Optional.empty()
                )),
                Optional.empty(),
                Optional.of(new ComponentSerialization.TextElement(
                        Component.literal("Edit in browser, preview renders here.\nPress ESC to exit editor mode."),
                        0.7f,
                        1,
                        Optional.empty()
                )),
                Optional.empty()
        );

        return new TipData(
                PREVIEW_ID,
                trigger,
                visual,
                behavior,
                List.of(page)
        );
    }

    private static TipData normalizeForPreview(TipData tip) {
        // Force stable id and persistent duration for editor preview.
        TipData.Behavior beh = tip.behavior();
        TipData.Behavior previewBehavior = new TipData.Behavior(
                -1,
                beh.pauseTimerOnHover(),
                beh.closableByKey(),
                beh.allowPaging()
        );
        return new TipData(
                PREVIEW_ID,
                tip.trigger(),
                tip.visualSettings(),
                previewBehavior,
                tip.pages()
        );
    }
}

