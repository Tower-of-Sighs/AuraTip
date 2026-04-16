package cc.sighs.auratip.dev;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.api.tip.TipBuilder;
import cc.sighs.auratip.api.tip.TipRegistry;
import cc.sighs.auratip.data.TipData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.List;

public final class DevJavaApiSamples {

    private static final String OWNER = "auratip_dev";
    public static final ResourceLocation DATAPACK_TIP = new ResourceLocation(AuraTip.MODID, "showtip_demo_intro");
    public static final ResourceLocation DATAPACK_MENU = new ResourceLocation(AuraTip.MODID, "example_menu");
    public static final ResourceLocation TRIGGER_SHOWTIP = new ResourceLocation(AuraTip.MODID, "showtip_command");
    public static final ResourceLocation TRIGGER_FIRST_JOIN = new ResourceLocation(AuraTip.MODID, "first_join_world");
    public static final ResourceLocation JAVA_TIP_SHOWTIP = new ResourceLocation(AuraTip.MODID, "dev_java_showtip");
    public static final ResourceLocation JAVA_TIP_FIRST_JOIN = new ResourceLocation(AuraTip.MODID, "dev_java_first_join");
    public static final ResourceLocation JAVA_TIP_BY_ID = new ResourceLocation(AuraTip.MODID, "dev_java_by_id");
    public static final ResourceLocation JAVA_MENU = new ResourceLocation(AuraTip.MODID, "dev_java_menu");
    public static final ResourceLocation JAVA_SCRIPT_ACTION = new ResourceLocation(AuraTip.MODID, "dev_action");

    private DevJavaApiSamples() {
    }

    public static void initCommon() {
        registerRuntimeTips();
        DevTestCommand.register();
    }

    private static void registerRuntimeTips() {
        TipData a = new TipBuilder(JAVA_TIP_SHOWTIP)
                .triggerRepeatable(TRIGGER_SHOWTIP, 0)
                .visual(v -> v
                        .animationStyle(new ResourceLocation(AuraTip.MODID, "slide_in_left"))
                        .hoverAnimationStyle(new ResourceLocation(AuraTip.MODID, "hover_float"))
                        .size(190, 60)
                        .positionAbsolute(12, 220)
                )
                .behavior(b -> b
                        .duration(160)
                        .pauseOnHover(true)
                )
                .page(0, p -> p
                        .title(Component.literal("Java Runtime Tip"), 0.85f, 0)
                        .content(Component.literal("由 Java 注册，/showtip 会触发我。\n玩家: ${player}"), 0.7f, 1)
                )
                .build();

        TipData b = new TipBuilder(JAVA_TIP_FIRST_JOIN)
                .triggerRepeatable(TRIGGER_FIRST_JOIN, 0)
                .visual(v -> v
                        .animationStyle(new ResourceLocation(AuraTip.MODID, "fade_and_slide"))
                        .hoverAnimationStyle(new ResourceLocation(AuraTip.MODID, "none"))
                        .size(180, 48)
                        .positionPreset("TOP_RIGHT")
                )
                .behavior(beh -> beh
                        .duration(120)
                        .pauseOnHover(false)
                )
                .page(0, p -> p
                        .title(Component.literal("登录触发"), 0.8f, 0)
                        .content(Component.literal("这是 Java runtime tip（触发: auratip:first_join_world）"), 0.65f, 1)
                )
                .build();

        TipData c = new TipBuilder(JAVA_TIP_BY_ID)
                .triggerRepeatable(new ResourceLocation(AuraTip.MODID, "unused_trigger"), 0)
                .visual(v -> v
                        .animationStyle(new ResourceLocation(AuraTip.MODID, "slide_in_right"))
                        .hoverAnimationStyle(new ResourceLocation(AuraTip.MODID, "hover_shake"))
                        .size(200, 55)
                        .positionPreset("CENTER")
                )
                .behavior(beh -> beh.duration(120))
                .page(0, p -> p
                        .title(Component.literal("triggerById 演示"), 0.8f, 0)
                        .content(Component.literal("用 /auratipdev tip trigger_id auratip:dev_java_by_id 点名触发"), 0.65f, 1)
                )
                .build();

        TipRegistry.setTips(OWNER, List.of(a, b, c));
    }
}
