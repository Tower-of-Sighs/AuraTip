package cc.sighs.auratip.handler;

import cc.sighs.auratip.api.client.TipClientApi;
import cc.sighs.auratip.client.RadialMenuClient;
import cc.sighs.auratip.client.render.RadialMenuOverlay;
import cc.sighs.auratip.client.render.TipOverlay;
import cc.sighs.auratip.dev.DevEnvironment;
import cc.sighs.auratip.dev.DevJavaApiSamples;
import cc.sighs.auratip.api.tip.TipBuilder;
import cc.sighs.oelib.event.Subscribe;
import cc.sighs.oelib.event.events.InputEvent;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;

public class ClientInputHandler {

    @Subscribe
    public static void onKeyInput(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }
        int key = event.getKey();
        if (TipOverlay.INSTANCE.isActive() && TipOverlay.INSTANCE.keyPressed(key)) {
            return;
        }
        if (RadialMenuOverlay.INSTANCE.isActive()) {
            RadialMenuOverlay.INSTANCE.keyPressed(key);
        }
    }

    @Subscribe
    public static void onMouseInput(InputEvent.MouseButton.Pre event) {
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }
        var minecraft = Minecraft.getInstance();
        var window = minecraft.getWindow();
        int guiWidth = window.getGuiScaledWidth();
        int guiHeight = window.getGuiScaledHeight();
        double guiX = minecraft.mouseHandler.xpos() * guiWidth / window.getScreenWidth();
        double guiY = minecraft.mouseHandler.ypos() * guiHeight / window.getScreenHeight();

        if (TipOverlay.INSTANCE.isActive() && TipOverlay.INSTANCE.mouseClicked(guiX, guiY, event.getButton())) {
            event.setCanceled(true);
            return;
        }
        if (RadialMenuOverlay.INSTANCE.isActive() && RadialMenuOverlay.INSTANCE.mouseClicked(guiX, guiY, event.getButton())) {
            event.setCanceled(true);
        }
    }

    @Subscribe
    public static void onRadialMenuKey(InputEvent.Key event) {
        if (event.getAction() != GLFW.GLFW_PRESS) {
            return;
        }
        var mc = Minecraft.getInstance();
        if (mc.screen != null) {
            return;
        }

        InputConstants.Key key = InputConstants.getKey(event.getKey(), event.getScanCode());

        if (DevEnvironment.isDev()) {
            if (ClientKeyMappings.DEV_TRIGGER_SHOWTIP.isActiveAndMatches(key)) {
                if (mc.player != null) {
                    mc.player.connection.sendCommand("showtip");
                }
                return;
            }

            if (ClientKeyMappings.DEV_ENQUEUE_CLIENT_TIP.isActiveAndMatches(key)) {
                if (mc.player != null) {
                    var tip = new TipBuilder(new ResourceLocation("auratip", "dev_client_enqueue"))
                            .triggerRepeatable(new ResourceLocation("auratip", "unused_trigger"), 0)
                            .visual(v -> v
                                    .animationStyle(new ResourceLocation("auratip", "fade_transition"))
                                    .hoverAnimationStyle(new ResourceLocation("auratip", "none"))
                                    .size(220, 55)
                                    .positionPreset("TOP_LEFT")
                            )
                            .behavior(b -> b.duration(160))
                            .page(0, p -> p
                                    .title(Component.literal("TipClientApi.enqueue"), 0.8f, 0)
                                    .content(Component.literal("这是客户端本地入队，不走服务器触发规则。\n玩家: ${player}"), 0.65f, 1)
                            )
                            .build();

                    TipClientApi.enqueue(List.of(tip), Map.of(
                            "player", mc.player.getDisplayName()
                    ));
                }
                return;
            }

            if (ClientKeyMappings.DEV_OPEN_DATAPACK_MENU.isActiveAndMatches(key)) {
                RadialMenuClient.openMenu(DevJavaApiSamples.DATAPACK_MENU);
                return;
            }

            if (ClientKeyMappings.DEV_OPEN_JAVA_MENU.isActiveAndMatches(key)) {
                RadialMenuClient.openMenu(DevJavaApiSamples.JAVA_MENU);
                return;
            }

            if (ClientKeyMappings.DEV_OPEN_KJS_MENU.isActiveAndMatches(key)) {
                RadialMenuClient.openMenu(new ResourceLocation("kubejs", "demo_menu"));
            }
        }
    }
}
