# AuraTip

---

AuraTip 是一个“可数据包配置 + 可 KubeJS 扩展”的提示面板（Tip）与径向菜单（Radial Menu）系统。

---

**Tip 数据包字段（详细）**

文件参考：[TipData.java](https://github.com/Tower-of-Sighs/AuraTip/blob/master/src/main/java/cc/sighs/auratip/data/TipData.java)

- 顶层
    - id：字符串，唯一标识
    - trigger：触发器，内置只有一个玩家进入世界的触发器，可用 Kubejs 脚本来添加更多触发器，详见下文。
    - visual_settings：可视化与动画
    - behavior：行为与交互
    - pages：页面列表（至少一页）

- trigger
    - type：触发器类型（例如 FIRST_JOIN_WORLD，大小写敏感，注册时什么样这里就原封不动搬过来）
    - mode：ONCE 或 REPEATABLE（大小写都可以）
    - cooldown：mode为可重复时的冷却时间（单位：tick）

- visual_settings（核心）
    - animation_style：进场/退场动画类型（如 fade_and_slide、slide_in_left…）
    - animation_speed：动画速度倍率（>0）
    - animation_from / animation_to：可选的“路径动画”起点/终点位置（可填预设字符串或绝对坐标）
    - hover_animation_style：悬浮动画类型（内置：none、hover_float、hover_shake 或自定义，大小写都可）
    - hover_animation_speed：悬浮动画速度倍率（>=0）
    - hover_only_on_hover：仅在鼠标悬停面板时启用悬浮动画（true\false）
    - stripe_width：主题色侧边条厚度（像素，默认 4）
    - stripe_length_factor：主题色侧边条长度占比（0~1，默认 1）
    - animation_params：进场动画的动态参数（具体参照下文内置动画参数，KJS添加的动画可自行添加参数）
    - hover_animation_params：悬浮动画的动态参数（具体参照下文内置动画参数，KJS添加的动画可自行添加参数）
    - background：背景
        - type：GRADIENT / SOLID / IMAGE
        - colors：颜色列表（type为 GRADIENT 可填两个或两个以上，SOLID 可填一个，argb）
        - border_radius：圆角像素
        - rounded：是否圆角
        - image_path：图片背景路径（type为 IMAGE 时填写）
    - theme_color：主题色（argb）
    - width / height：面板尺寸
    - position：面板位置（预设或绝对坐标）

- behavior
    - default_duration：默认展示时长（tick），-1 表示常驻直到关闭
    - pause_timer_on_hover：鼠标悬停是否暂停计时
    - closable_by_key：可选，按键名，按下该按键可关闭 Tip
    - allow_paging：是否允许翻页

- pages（可写多个）
    - page_index：页序号（必须唯一）
    - title / subtitle / content：文本元素（可选）
        - 内有 text、scale、line_spacing、divider
    - image：图片元素（可选）
        - path：纹理路径（eg. minecraft:textures/item/......）
        - position：图片位置（预设或绝对坐标）
        - size：宽高
        - scale：缩放

小 Tip：

- **Position 位置写法（二选一）**
    - 预设：`"position": "BOTTOM_CENTER"`（必须大写）
    - 绝对坐标：`"position": [10, 200]`
    - 可用预设：`TOP_CENTER` `TOP_LEFT` `TOP_RIGHT` `BOTTOM_LEFT` `BOTTOM_RIGHT` `BOTTOM_CENTER` `CENTER` / `MIDDLE`

- **text 字段写法（二选一）**

最简单：

```json
"text": "祝你玩的开心！"
```

带样式（示例只写常用项，能用什么以 Style
的序列化为准：color、bold、italic、underlined、strikethrough、obfuscated、clickEvent、hoverEvent、insertion、font）：

```json
"text": {
  "text": "欢迎来到整合包",
  "color": "#55FFFF"
}
```

- **divider（分割线）字段**
    - `thickness`：厚度（像素）
    - `margin_top`：上边距（像素）
    - `margin_bottom`：下边距（像素）
    - `length`：长度比例（0~1）
    - `color`：颜色（argb，例如 `"#DD12121F"`）

```json
"divider": {
  "thickness": 1,
  "margin_top": 2,
  "margin_bottom": 4,
  "length": 0.38,
  "color": "#DD12121F"
}
```

**Radial Menu 数据包字段（详细）**

文件参考：[RadialMenuData.java](https://github.com/Tower-of-Sighs/AuraTip/blob/master/src/main/java/cc/sighs/auratip/data/RadialMenuData.java) ·
渲染参考：[RadialMenuOverlay.java](https://github.com/Tower-of-Sighs/AuraTip/blob/master/src/main/java/cc/sighs/auratip/client/render/RadialMenuOverlay.java)

- 顶层
    - menu_settings：菜单基础样式
    - slots：槽位列表（至少一个）

- menu_settings
    - inner_radius：内半径，像素
    - outer_radius：外半径，像素（必须大于 inner）
    - animation_speed：开合动画速度倍率（>0，越大越快）
    - center_icon：中心图标（可选，ResourceLocation）
    - ring_color：轮盘颜色（可选，单色，argb）
    - ring_colors：轮盘颜色（渐变版，可选，列表 argb）

- slots（每个槽位）
    - name：槽位名称
    - icon：图标纹理（ResourceLocation）
    - action：行为对象（动态结构，详见下文）
    - text：槽位文本（可选，Component）
    - highlight_color：鼠标悬停高亮颜色（可选）

- action（动态结构）
    - 采用 type + 任意参数的模型，内置run_command，可使用KJS注册任意action，下文有说明。

```json
{
  "type": "run_command",
  "command": "effect give @p minecraft:instant_health 1 1"
}
```

示例（最小可用）：

```json
{
  "id": "welcome_tip",
  "trigger": {
    "type": "SHOWTIP_COMMAND",
    "mode": "repeatable",
    "cooldown": 0
  },
  "visual_settings": {
    "animation_style": "SLIDE_IN_LEFT",
    "animation_params": {
      "extra_padding": 100.0
    },
    "hover_animation_style": "hover_float",
    "hover_animation_params": {
      "amplitude": 5.0,
      "ramp_duration": 0.2
    },
    "background": {
      "type": "gradient",
      "colors": [
        "#EE1A1A2E",
        "#DD12121F"
      ],
      "border_radius": 6,
      "rounded": true
    },
    "theme_color": "#5C6BC0",
    "width": 180,
    "height": 55,
    "position": [
      10,
      200
    ],
    "animation_speed": 1.2,
    "animation_from": [
      0,
      200
    ],
    "animation_to": [
      10,
      200
    ]
  },
  "behavior": {
    "default_duration": 200,
    "pause_timer_on_hover": true,
    "closable_by_key": "key.keyboard.delete",
    "allow_paging": true
  },
  "pages": [
    {
      "page_index": 0,
      "title": {
        "text": {
          "text": "欢迎来到整合包"
        },
        "scale": 0.8
      },
      "content": {
        "text": {
          "text": "祝你玩的开心！"
        }
      }
    }
  ]
}
```

---

**KubeJS 事件与 API** （作者 KubeJS 经验尚浅，欢迎纠错）

- TipEvents.register：脚本构建
  Tip（[TipRegistrationEvent.java](https://github.com/Tower-of-Sighs/AuraTip/blob/master/src/main/java/cc/sighs/auratip/compat/kubejs/tip/TipRegistrationEvent.java)）
-

RadialMenuEvents.register：脚本构建轮盘（[RadialMenuRegistrationEvent.java](https://github.com/Tower-of-Sighs/AuraTip/blob/master/src/main/java/cc/sighs/auratip/compat/kubejs/radiamenu/RadialMenuRegistrationEvent.java)）
-
TipVars（TipVariables）：注册/快照变量（[TipVariables.java](https://github.com/Tower-of-Sighs/AuraTip/blob/master/src/main/java/cc/sighs/auratip/compat/kubejs/tip/TipVariables.java)）

- TipTriggers：主动触发
  tip（[TipTriggers.java](https://github.com/Tower-of-Sighs/AuraTip/blob/master/src/main/java/cc/sighs/auratip/compat/kubejs/tip/TipTriggers.java)）
-

TipText：构造富文本（[TipText.java](https://github.com/Tower-of-Sighs/AuraTip/blob/master/src/main/java/cc/sighs/auratip/compat/kubejs/tip/TipText.java)）
-
TipAnimations：注册动画工厂（[TipAnimations.java](https://github.com/Tower-of-Sighs/AuraTip/blob/master/src/main/java/cc/sighs/auratip/compat/kubejs/tip/TipAnimations.java)）
-
Actions：注册动作与构建动作对象（[ActionsKJS.java](https://github.com/Tower-of-Sighs/AuraTip/blob/master/src/main/java/cc/sighs/auratip/compat/kubejs/radiamenu/action/ActionsKJS.java)）
-
RadialMenus：运行时追加轮盘槽位（[RadialMenusKJS.java](https://github.com/Tower-of-Sighs/AuraTip/blob/master/src/main/java/cc/sighs/auratip/compat/kubejs/radiamenu/slot/RadialMenusKJS.java)）

示例（Tip，完整链路）

```javascript
TipEvents.register(event => {
    // 数据包 showtip_demo 的 KJS 版
    const tip = event.create('showtip_demo_intro');

    // 1. 设置触发器 (Trigger)
    // 参数: 类型, 模式, 冷却时间
    tip.trigger('SHOWTIP_COMMAND', 'repeatable', 0);

    // 2. 设置视觉样式 (Visual Settings)
    tip.visual(v => {
        v.animationStyle('SLIDE_IN_LEFT')
            .animationSpeed(1.2)
            .size(180, 55)
            .position(10, 200)
            .animationFrom(0, 200)
            .animationTo(10, 200)
            .themeColor('#5C6BC0')
            .background('gradient', ['#EE1A1A2E', '#DD12121F'], 6)
            .backgroundRounded(true)
            .hoverAnimationStyle('hover_float')
            .hoverAnimationSpeed(1.0)
            .animParam('extra_padding', 100.0)
            .hoverParam('amplitude', 5.0)
            .hoverParam('ramp_duration', 0.2);
    });

    // 3. 设置行为 (Behavior)
    tip.behavior(b => {
        b.duration(200)
            .pauseOnHover(true)
            .closeKey('key.keyboard.delete')
            .allowPaging(true);
    });

    // 4. 设置页面 (Pages)

    // 第一页：介绍
    tip.page(0, p => {
        p.title(TipText.of("AuraTip · 测试").formatting("aqua").bold().build(), 0.8, 0)
            .subtitle(TipText.of("指令触发演示").formatting("gray").build(), 0.7, 0)
            .content(TipText.of("● 样式丰富 ● 动画丝滑").build(), 0.65, 1)
            .image('minecraft:textures/item/clock_00.png', 'LEFT_CENTER', 20, 20);
    });

    // 第二页：变量展示
    tip.page(1, p => {
        p.title(TipText.of("实时变量").formatting("yellow").bold().build(), 0.8, 0)
            .titleDivider(1, 2, 4, 0.38, null) // thickness, marginTop, marginBottom, length, color
            .content(TipText.of("玩家: ${player}\n坐标: ${x}, ${z}").build(), 0.7, 1)
            .image('minecraft:textures/item/compass_05.png', 'LEFT_CENTER', 18, 18);
    });

    // 第三页：主题边缘
    tip.page(2, p => {
        p.title(TipText.of("主题边缘").formatting("light_purple").bold().build(), 0.8, 0)
            .content(TipText.of("左侧竖条自动渐变\n适配小比例面板").build(), 0.65, 1);
    });
});
```

示例（Radial Menu，完整链路）

```javascript
const invAction = Actions.of('open_gui', {screen: 'inventory_screen'});
const ftbAction = Actions.of('open_gui', {screen: 'ftb_quest_screen'});
const timeAction = Actions.of('run_command', {command: 'time set day'});
const healthAction = Actions.of('run_command', {command: 'effect give @p minecraft:instant_health 1 1'});
RadialMenuEvents.register(event => {
    // 数据包 example 的KJS版
    const menu = event.create('example_menu');

    // 1. 设置菜单基础参数 (Radii, Animation, Colors)
    menu.radii(55, 100)
        .animationSpeed(0.2)
        .ringColors([
            "#1A050A10",
            "#D9102030"
        ]);

    // 2. 添加槽位 (Slots)
    // .slot(名称, 图标资源路径, 动作对象, 文本组件, 高亮颜色)

    // 槽位 1: 打开背包
    menu.slot(
        "Inventory",
        "minecraft:textures/item/apple.png",
        invAction,
        TipText.translatable("tip.auratip.open_inventory").colorHex("#55FFFF").bold().underlined().build(),
        "#EEEEEE"
    );

    // 槽位 2: FTB Quests
    menu.slot(
        "FTB Quests",
        "ftbquests:textures/item/book.png",
        ftbAction,
        TipText.translatable("tip.auratip.open_quests").colorHex("#55FFFF").bold().underlined().build(),
        "#FFD700"
    );

    // 槽位 3: 设置白天
    menu.slot(
        "Set Day",
        "minecraft:textures/item/clock_06.png",
        timeAction,
        TipText.translatable("tip.auratip.set_day").colorHex("#55FFFF").underlined().build(),
        "#FFFACD"
    );

    // 槽位 4: 治疗
    menu.slot(
        "Health",
        "minecraft:textures/item/golden_apple.png",
        healthAction,
        TipText.translatable("tip.auratip.heal").colorHex("#55FFFF").underlined().build(),
        "#FF69B4"
    );
});
```

---

**非事件 KubeJS 工具（运行时直接调用）**

这些 API 不依赖注册事件，在任意脚本里都能用来“拼装对象/注册扩展”。

**TipText 静态方法**

- `TipText.of(base)`：把字符串/数字/Component 包装成 Builder（base 传 Component 会复制一份）
- `TipText.translatable(key, ...args)`：创建一个可翻译文本（相当于原版的 translatable）
- `TipText.join(...parts)`：拼接多段文本，parts 可以是 Builder / Component / 其它对象（其它对象会转成字符串）

**Builder 可链式调用的方法**

- `colorHex("#RRGGBB" | "#AARRGGBB")`
- `colorRgb(0xRRGGBB)`
- `formatting("aqua" | "yellow" | "bold" | ... )`（内部会按 ChatFormatting 名字解析；写错会被忽略）
- `bold()` / `italic()` / `underlined()` / `strikethrough()` / `obfuscated()`
- `appendText("...")` / `appendComponent(otherComponent)`
- `build()`：拿到最终的 Component（当某个 API 要求传 Component 时一定要 build）

```javascript
// TipBuilder.page().title/subtitle/content 这类方法需要 Component，所以最后要 .build()
const title = TipText.of('Hello').formatting('aqua').bold().build();

// join 里可以直接塞 Builder，它会自动 build
const line = TipText.join('玩家：', TipText.of('${player}').formatting('yellow'));
```

- Actions.register：注册一个“自定义动作类型”（用于 Radial Menu 槽位 action）

```javascript
Actions.register('open_gui', params => {
    const Minecraft = Java.loadClass('net.minecraft.client.Minecraft');
    const InventoryScreen = Java.loadClass('net.minecraft.client.gui.screens.inventory.InventoryScreen');

    const dyn = params.get('screen');
    if (dyn == null) return;

    const screen = dyn.asString('');
    if (screen === 'inventory_screen') {
        const mc = Minecraft.getInstance();
        mc.setScreen(new InventoryScreen(mc.player));
    }
});

const openInv = Actions.of('open_gui', {screen: 'inventory_screen'});
```

这里的 `params` 是“动作对象 action 里除了 type 之外的所有字段”。

- `open_gui` 是 action 的 **type 字段**（也就是 `Actions.of('open_gui', ...)` 的第一个参数）
- `screen` 是 action 的 **参数字段名**（也就是 `Actions.of('open_gui', { screen: 'inventory_screen' })` 这个对象里的 key）

对照起来更直观：

```javascript
// 构造一个动作对象：type=open_gui，参数里有一个字段叫 screen
const action = Actions.of('open_gui', {screen: 'inventory_screen', debug: true, speed: 1.25});

Actions.register('open_gui', params => {
    // 这里拿到的 params 就是：{ screen: ..., debug: ..., speed: ... }
    const dynScreen = params.get('screen');
    const screen = dynScreen == null ? '' : dynScreen.asString('');

    const dynDebug = params.get('debug');
    const debug = dynDebug == null ? false : dynDebug.asBoolean(false);

    const dynSpeed = params.get('speed');
    const speed = dynSpeed == null ? 1.0 : dynSpeed.asDouble(1.0);
});
```

你只需要记住一条：**读不到/不合法就用 fallback**。

- `dyn.asString('')`：按字符串读；失败就返回空串
- `dyn.asDouble(1.0)`：按数字读；失败就返回 1.0
- `dyn.asBoolean(false)`：按布尔读；失败就返回 false

- Actions.of：构造动作对象（给 Radial Menu 槽位用）

```javascript
const action = Actions.of('open_gui', {screen: 'inventory_screen'});
```

- TipVars：注册 tip 变量（服务端），用于文本里的 ${变量} 替换

```javascript
TipVars.register('server_name', 'MyPack'); // 我可以在 数据包 / KJS 中这样写：${server_name}，会被替换为MyPack。
TipVars.registerDynamic('player', () => global.getCurrentPlayerName());
```

- TipTriggers：注册一个 Tip 触发器（服务端）

```javascript
TipTriggers.trigger('SHOWTIP_COMMAND', player);
```

- TipAnimations.register / registerHover：注册“自定义动画工厂”（客户端）

这类工厂的返回值就是一组“计算函数”。引擎会在渲染时不停调用它们，用你返回的结果来决定：

普通过场动画

- easedProgress：动画走到哪一步（0~1）
- offsetX / offsetY：当前位置要偏移多少像素
  悬浮动画
- offsetX / offsetY：当前位置要偏移多少像素

```javascript
TipAnimations.register('kjs_bounce_left', params => {
    // 这里和上述 Action 部分一样，这样写就代表该动画的参数有 overshoot 和 extra_distance
    // 数据类型是 double，出错时 fallback 到 1.70158 和 50
    const overshoot = getDouble(params, 'overshoot', 1.70158);
    const extraDistance = getDouble(params, 'extra_distance', 50);

    return {
        easedProgress: (now, start, closing, openMs, closeMs) => {
            const duration = closing ? closeMs : openMs;
            if (duration <= 0) return closing ? 0.0 : 1.0;

            let t = (now - start) / duration;
            if (t <= 0.0) return closing ? 0.0 : 0.0;
            if (t >= 1.0) return closing ? 0.0 : 1.0;

            if (closing) {
                const u = 1.0 - t;
                return u * u;
            }

            t = t - 1.0;
            return t * t * ((overshoot + 1.0) * t + overshoot) + 1.0;
        },

        offsetX: (eased, w, h) => {
            const distance = w + extraDistance;
            return Math.floor(distance * (1.0 - eased));
        },

        offsetY: () => 0
    };
});

TipAnimations.registerHover('kjs_jelly', params => {

    const ampX = getDouble(params, 'amplitude_x', 1.5);
    const ampY = getDouble(params, 'amplitude_y', 3.0);
    const freqX = getDouble(params, 'frequency_x', 1.2);
    const freqY = getDouble(params, 'frequency_y', 0.8);
    const rampDuration = getDouble(params, 'ramp_duration', 1.0);

    const TWO_PI = Math.PI * 2.0;

    return {
        offsetX: (now, start, w, h, speed) => {
            const seconds = Math.max(0, now - start) / 1000.0;
            const effectiveSpeed = speed <= 0.0 ? 1.0 : speed;
            const ramp = Math.min(seconds / rampDuration, 1.0);

            const angle = seconds * TWO_PI * freqX * effectiveSpeed;
            return Math.round(Math.sin(angle) * ampX * ramp);
        },

        offsetY: (now, start, w, h, speed) => {
            const seconds = Math.max(0, now - start) / 1000.0;
            const effectiveSpeed = speed <= 0.0 ? 1.0 : speed;
            const ramp = Math.min(seconds / rampDuration, 1.0);

            const angle = seconds * TWO_PI * freqY * effectiveSpeed;
            return Math.round(Math.sin(angle) * ampY * ramp);
        }
    };
});

function getDouble(params, key, fallback) {
    const v = params?.get?.(key);
    if (!v) return fallback;
    return v.asDouble(fallback);
}
```
