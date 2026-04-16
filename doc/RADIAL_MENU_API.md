本篇会带你用 **Java API** 创建一个轮盘，非常简单，几行代码即可。
> 注：本篇文档假设你有一定的 Forge Mod 开发基础或者 Kubejs 基础，至少能看懂本篇文档的代码语法。

嗯...先引入依赖吧：
```groovy
repositories {
    maven {
        url "https://maven.sighs.cc/repository/maven-releases/"
    }
    maven {
        url "https://maven.sighs.cc/repository/maven-snapshots/"
    }
}

dependencies {
    // ModDevGradle
    modImplementation ('cc.sighs.oelib:OELib-forge-1.20.1:0.2.4-20260413.075423-2')
    modImplementation ('cc.sighs:AuraTip-forge-1.20.1:1.0.0-beta')

    // ForgeGradle
    implementation fg.deobf("cc.sighs.oelib:OELib-forge-1.20.1:0.2.4-20260413.075423-2")
    implementation fg.deobf("cc.sighs:AuraTip-forge-1.20.1:1.0.0-beta")
}

```

---
好，正式开始，首先我们新建一个类，就这样：
```java
import cc.sighs.auratip.api.action.Actions;
import cc.sighs.auratip.api.radiamenu.RadialMenuBuilder;
import cc.sighs.auratip.api.radiamenu.RadialMenuRegistry;
import cc.sighs.auratip.data.RadialMenuData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ModRadialMenu {
    public static final ResourceLocation MENU_ID = new ResourceLocation(ExampleMod.MODID, "my_menu");

    public static void registerMyMenu() {
        RadialMenuData menu = new RadialMenuBuilder(MENU_ID)
                .radii(55, 100) // 内半径/外半径
                .animationSpeed(1.0f) // 唤出/收起的动画速度
                .ringColors(List.of("#1A071B10", "#D91A6B3A")) // 轮盘整体颜色
                .slot(
                        "Day", // slot 名称，在插入槽位的时候，相同名称的槽位会被覆盖
                        new ResourceLocation(ResourceLocation.DEFAULT_NAMESPACE, "textures/item/clock_06.png"), // 该槽位的图标纹理
                        Actions.runCommand("/time set day"), // 执行设置时间的动作
                        Component.literal("Set Day"), // 鼠标悬浮在该槽位，轮盘中心显示的信息
                        "#77FFFFFF" // 鼠标悬浮高亮颜色
                )
                .build();
        RadialMenuRegistry.setMenus(ExampleMod.MODID, List.of(menu)); // 注册
    }
}
```
然后在客户端主类调用此方法：
```java
public class ExampleModClient {
    public static void init() {
        ModAction.registerMyAction();
        ModRadialMenu.registerMyMenu();
        ClientKeyMapping.register();
    }
}

@Mod(ExampleMod.MODID)
public class ExampleMod {
    public static final String MODID = "examplemod";

    public ExampleMod() {
        DistExecutor.safeRunWhenOn(Dist.CLIENT, () -> ExampleModClient::init);
    }
}
```

好，那么你的轮盘就是这样的：

![示例轮盘](https://resource-api.xyeidc.com//client/members/pics/86526a34)

>对应的 Kubejs 代码：
>```javascript
>// client_scripts/example.js
>
>RadialMenuEvents.register(event => {
>    event.create('examplemod:my_menu')
>    .radii(55, 100)
>    .animationSpeed(1.0)
>   .ringColors(['1A071B10', 'D91A6B3A'])
>    .slot(
>        'Day',
>        "minecraft:textures/item/clock_06.png", 
>        Actions.of('run_command', {command: '/time set day'}), 
>        Text.of('Set Day'), 
>        '#77FFFFFF'
>    )
>})
>
>```

---
如何打开呢？ `RadialMenuClientApi` 提供了一个 `open(ResourceLocation menuId)` 方法，我们可以：
```java
        if (ClientKeyMapping.OPEN_MENU.isActiveAndMatches(key)) {
            // 如果是 Kubejs 注册的轮盘，modid 就是 `kubejs`，数据包添加的轮盘 `modid` 就是本模组的 `auratip`。
            RadialMenuClientApi.open(ModRadialMenu.MENU_ID);
        }
```
这样通过按键来打开它，或者其他什么方式也可以。

>对应的 Kubejs 代码：
>```javascript
>// client_scripts/example_test.js
>// 注：在1.20.1中，此写法需安装 EventJS
>
>let $EventPriority = Java.loadClass('net.minecraftforge.eventbus.api.EventPriority')
>let $InputEventKey = Java.loadClass('net.minecraftforge.client.event.InputEvent$Key')
>NativeEvents.onEvent(
>    $EventPriority.NORMAL,
>    false, 
>    $InputEventKey, 
>    event => {
>        if(event.action != 1) return
>
>        if(event.key !== 82) return
>        RadialMenus.open('examplemod:my_menu')
>    }
>)
>
>```

---
内置只有两个动作，一个是执行命令，一个则是执行按键逻辑（目前只有极少数按键有实际效果）

当然，你也可以注册自己的动作，如下：
```java
import cc.sighs.auratip.api.action.Actions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;

/**
 * 注意：这里仅展示 `ScripeAcion` 的注册方法，不会被序列化，也就是数据包不能用你这个 Action
 * 如果想让数据包也可以用，请使用 Actions.registerCodec(type, actionClass, codec) 方法，并实现`Action`接口
 */
public class ModAction {
    public static final ResourceLocation MY_ACTION = new ResourceLocation(ExampleMod.MODID, "open_screen");

    // 此方法也要在客户端主类调用
    public static void registerMyAction() {
        Actions.register(MY_ACTION, params -> {
            var mc = Minecraft.getInstance();
            if (mc.player == null) return;
            String screenId = params.getString("screenId", "");
            if ("inventory_screen".equals(screenId)) {
                var inv = new InventoryScreen(mc.player);
                mc.setScreen(inv);
            }
        });
    }
}
```
来给刚才的轮盘加一个新槽位：
```java
                .slot(
                        "Inventory Screen",
                                new ResourceLocation(ResourceLocation.DEFAULT_NAMESPACE, "textures/item/apple.png"),
                        Actions.script(
        ModAction.MY_ACTION,
        Map.of("screenId", "inventory_screen")
                        ),
                                Component.literal("Open Inventory Screen"),
                        "#FFFACD"
                                )
```
大功告成，我们的轮盘现在有两个槽位了：

![示例轮盘2](https://resource-api.xyeidc.com//client/members/pics/35fe06ca)

>对应的 Kubejs 代码：
>```javascript
>// client_scripts/example_action.js
>
>let $Minecraft = Java.loadClass('net.minecraft.client.Minecraft')
>let $InventoryScreen = >Java.loadClass('net.minecraft.client.gui.screens.inventory.InventoryScreen')
>
>Actions.register('open_screen', params => {
>    let mc = $Minecraft.getInstance()
>    if (!mc || !mc.player) return
>
>    let screen = params.get('screenId').asString('')
>
>    if(screen === 'inventory_screen') {
>        let inv = new $InventoryScreen(mc.player)
>        mc.setScreen(inv)
>    }
>})
>
>// client_scripts/example.js
>    .slot(
>        'Inventory Screen',
>        'minecraft:textures/item/apple.png',
>        Actions.of('open_screen', {screenId: 'inventory_screen'}),
>        Text.of('Open Inventory Screen'),
>        '#FFFACD'
>    )
>    
>```

---
如果你想要给轮盘动态附加一个槽位也是可以的，只需要使用 `RadialMenuExtraSlots` ：
```java
import cc.sighs.auratip.api.action.Actions;
import cc.sighs.auratip.api.radiamenu.RadialMenuExtraSlots;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class ModExtraSlot {

    // 给我们刚才的轮盘附加一个槽位
    public static void appendSlotForMenu() {
        RadialMenuExtraSlots.addSlotForMenu(
                ModRadialMenu.MENU_ID,
                "My Extra Slot",
                new ResourceLocation(ResourceLocation.DEFAULT_NAMESPACE, "textures/item/diamond.png"),
                Actions.runCommand("/say extra slot"),
                Component.literal("extra"),
                "#77FFFFFF"
        );
    }
}
```
我们要在击杀僵尸后加上这个槽位：
```java
@Mod.EventBusSubscriber(modid = ExampleMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModEventHandler {

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer)) {
            return;
        }
        if (!(event.getEntity() instanceof Zombie)) {
            return;
        }
        ModExtraSlot.appendSlotForMenu();
    }
}
```
![轮盘示例3](https://resource-api.xyeidc.com//client/members/pics/d688d829)

想移除也是可以的，`RadialMenuExtraSlots`中有 `removeSlotForMenu` 方法可以用，和添加差不多，这里就不讲了。

>对应的 Kubejs 代码：
>```javascript
>// server_scripts/example_extra_trigger
>
>EntityEvents.death(event => {
>    let player = event.source.player
>    if (!player) return
>
>    if (event.entity.type !== 'minecraft:zombie') return
>    appendSlotForMenu()
>})
>
>function appendSlotForMenu() {
>    RadialMenus.addSlot(
>        'examplemod:my_menu',
>        'My Extra Slot',
>        'minecraft:textures/item/diamond.png',
>        Actions.of('run_command', { command: '/say extra slot' }),
>        Text.of('extra'),
>        '#77FFFFFF'
>    )
>}
>
>```

看到这里，恭喜你！你已经学会了轮盘的所有用法。