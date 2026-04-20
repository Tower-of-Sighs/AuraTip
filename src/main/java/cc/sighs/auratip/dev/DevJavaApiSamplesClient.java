package cc.sighs.auratip.dev;

import cc.sighs.auratip.api.action.Actions;
import cc.sighs.auratip.api.radiamenu.RadialMenuBuilder;
import cc.sighs.auratip.api.radiamenu.RadialMenuRegistry;
import cc.sighs.auratip.data.RadialMenuData;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;

public final class DevJavaApiSamplesClient {

    private static final String OWNER = "auratip_dev";

    private DevJavaApiSamplesClient() {
    }

    public static void initClient() {
        registerDevScriptActionHandler();
        registerRuntimeMenu();
    }

    private static void registerRuntimeMenu() {
        RadialMenuData menu = new RadialMenuBuilder(DevJavaApiSamples.JAVA_MENU)
                .radii(55, 100)
                .ringColors(List.of("#1A1A0E2A", "#D95C2B8F"))
                .slot(
                        "ShowTip (/showtip)",
                        Identifier.fromNamespaceAndPath("minecraft", "textures/item/paper.png"),
                        Actions.runCommand("/showtip"),
                        Component.literal("/showtip"),
                        "#77FFFFFF"
                )
                .slot(
                        "NJS Action (open inventory)",
                        Identifier.fromNamespaceAndPath("minecraft", "textures/item/apple.png"),
                        Actions.script(Identifier.fromNamespaceAndPath("nekojs", "open_gui"), Map.of("screen", "inventory_screen")),
                        Component.literal("nekojs:open_gui"),
                        "#77FFFFFF"
                )
                .slot(
                        "Java Script Action",
                        Identifier.fromNamespaceAndPath("minecraft", "textures/item/diamond.png"),
                        Actions.script(DevJavaApiSamples.JAVA_SCRIPT_ACTION, Map.of("message", "Hello from Java runtime menu")),
                        Component.literal("auratip:dev_action"),
                        "#77FFFFFF"
                )
                .build();

        RadialMenuRegistry.setMenus(OWNER, List.of(menu));
    }

    private static void registerDevScriptActionHandler() {
        Actions.register(DevJavaApiSamples.JAVA_SCRIPT_ACTION, params -> {
            var mc = Minecraft.getInstance();
            if (mc.player == null) {
                return;
            }
            String msg = params.getString("message", "");
            if (msg.isBlank()) {
                msg = "(empty message)";
            }
            mc.player.sendSystemMessage(Component.literal("[AuraTip Dev] " + msg));
        });
    }
}
