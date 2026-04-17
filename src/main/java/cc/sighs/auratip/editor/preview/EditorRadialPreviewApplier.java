package cc.sighs.auratip.editor.preview;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.client.render.RadialMenuOverlay;
import cc.sighs.auratip.data.RadialMenuData;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class EditorRadialPreviewApplier {
    private static final ResourceLocation PREVIEW_ID = AuraTip.id("editor_preview_menu");

    private EditorRadialPreviewApplier() {
    }

    public static RadialMenuData defaultMenu() {
        RadialMenuData.MenuSettings settings = new RadialMenuData.MenuSettings(
                55,
                100,
                1.0f,
                java.util.Optional.empty(),
                java.util.Optional.of("#CC101010"),
                java.util.Optional.empty()
        );

        RadialMenuData.Slot slot = new RadialMenuData.Slot(
                "Preview",
                ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/paper.png"),
                new cc.sighs.auratip.data.action.Action.RunCommand("/say AuraTip Editor Preview"),
                java.util.Optional.of(net.minecraft.network.chat.Component.literal("Preview")),
                java.util.Optional.of("#77FFFFFF")
        );

        return new RadialMenuData(PREVIEW_ID, settings, List.of(slot));
    }

    public static void applyDefaultPreview() {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.player == null || mc.level == null) {
                return;
            }
            var window = mc.getWindow();
            RadialMenuOverlay.INSTANCE.open(defaultMenu(), window.getGuiScaledWidth(), window.getGuiScaledHeight(), mc);
        });
    }

    public static void closePreview() {
        Minecraft.getInstance().execute(() -> RadialMenuOverlay.INSTANCE.close());
    }

    public static void applyMenuJson(JsonElement menuJson) {
        if (menuJson == null) {
            return;
        }

        DataResult<RadialMenuData> parsed = RadialMenuData.CODEC.parse(JsonOps.INSTANCE, menuJson);
        RadialMenuData menu = parsed.resultOrPartial(msg -> AuraTip.LOGGER.warn("Editor radial menu parse error: {}", msg)).orElse(null);
        if (menu == null) {
            return;
        }

        // Force stable id for preview to avoid any trigger/registry bookkeeping.
        RadialMenuData preview = new RadialMenuData(PREVIEW_ID, menu.menuSettings(), menu.slots());

        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.player == null || mc.level == null) {
                return;
            }
            var window = mc.getWindow();
            RadialMenuOverlay.INSTANCE.open(preview, window.getGuiScaledWidth(), window.getGuiScaledHeight(), mc);
        });
    }
}
