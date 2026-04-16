# Java Tip API
本篇会带你用 **Java API** 写一个 Tip（提示面板），并把它“注册、触发、显示”完整走一遍。

如果你是整合包作者，通常更推荐先看 `DATAPACK_TIP.md` / `KJS_TIP_API.md`；但只要你愿意写点 Java，这套 API 会更自由：你可以在任何事件、任何逻辑里决定 **什么时候给谁弹什么 Tip**。

---

## 你需要先理解的 3 个概念

### 1) TipData：最终的数据
Tip 的“真身”是 `cc.sighs.auratip.data.TipData`，它包含：

- `id`：全局唯一的 id（`ResourceLocation`）
- `trigger`：触发类型、触发模式（一次/可重复）、冷却
- `visual_settings`：尺寸、位置、背景、动画、主题色等
- `behavior`：持续时间、关闭方式、是否允许翻页
- `pages`：页面列表（每页可以有标题/副标题/正文/图片）

你可以通过两种来源提供 `TipData`：

- **数据包**：`data/auratip/tips/*.json`（见 `DATAPACK_TIP.md`）
- **运行时（Java/KubeJS）**：用 `TipBuilder` 组装，再放进 `TipRegistry`

> 注意：Tip 的 `id` **必须全局唯一**。

### 2) TipRegistry：运行时 Tip 的“仓库”
`cc.sighs.auratip.api.tip.TipRegistry` 用来管理“运行时注册的 Tip”。

- 一个 owner（填你的 `modid`）对应一组 tips
- `kubejs` 这个是用 Kubejs 添加的 tips 自带的 id，非必要不要用，除非你的模组叫 kubejs。

### 3) TipServer：在服务器端触发 / 直接显示
`cc.sighs.auratip.api.tip.TipServer` 是 **服务器侧**入口：

- `trigger(type, player, vars)`：按 trigger.type 匹配并触发（会走 ONCE/冷却规则）
- `triggerById(tipId, player, vars)`：点名触发某个 tip id（也会走 ONCE/冷却规则）
- `show(player, tips, vars)`：直接推送，不走触发过滤/冷却（“立刻显示”）

---

## 快速上手：写一个最小可用 Tip（Java 运行时）
下面这段是最常见的写法：

1. 用 `TipBuilder` 构建 出 `TipData`
2. 用 `TipRegistry.setTips(owner, ...)` 注册
3. 在某个事件里 `TipServer.trigger(...)`

```java
import cc.sighs.auratip.api.tip.TipBuilder;
import cc.sighs.auratip.api.tip.TipRegistry;
import cc.sighs.auratip.api.tip.TipServer;
import cc.sighs.auratip.data.TipData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Map;

public final class MyTips {
    public static final String OWNER = "mymod";

    public static final ResourceLocation TIP_ID = new ResourceLocation("mymod", "intro");
    public static final ResourceLocation TRIGGER_ID = new ResourceLocation("mymod", "player_login");

    public static void registerRuntimeTips() {
        TipData tip = new TipBuilder(TIP_ID)
                .triggerOnce(TRIGGER_ID)
                .visual(v -> v
                        .size(220, 55)
                        .positionPreset("TOP_LEFT")
                        .animationStyle(new ResourceLocation("auratip", "fade_and_slide"))
                        .hoverAnimationStyle(new ResourceLocation("auratip", "none"))
                )
                .behavior(b -> b
                        .duration(160)
                        .pauseOnHover(true)
                )
                .page(0, p -> p
                        .title(Component.literal("欢迎！"), 0.8f, 0)
                        .content(Component.literal("你好，${player}"), 0.65f, 1)
                )
                .build();

        TipRegistry.setTips(OWNER, List.of(tip));
    }

    public static void triggerLoginTip(ServerPlayer player) {
        TipServer.trigger(TRIGGER_ID, player, Map.of(
                "player", player.getDisplayName()
        ));
    }
}
```

> 你看到的 `${player}` 不是魔法：AuraTip 会在渲染时用 variables map 做字符串占位替换。

---

## TipBuilder：把 TipData “拼出来”
核心类：`cc.sighs.auratip.api.tip.TipBuilder`

### 1) 构造器与 id 规则

- `new TipBuilder(ResourceLocation id)`
- 这个 id 用来做 “ONCE 是否展示过 / REPEATABLE 冷却” 的记录，所以建议 **稳定且长期不变**

### 2) trigger：触发信息

- `trigger(type, mode, cooldownTicks)`
- `triggerOnce(type)`：等价于 `mode=ONCE`
- `triggerRepeatable(type, cooldownTicks)`：等价于 `mode=REPEATABLE`

触发规则在服务端执行，记录存放在玩家 `persistentData` 下的 `auratip_shown_tips` 里。

> AuraTip 自带两个常用触发类型：
> - `auratip:first_join_world`：玩家登录时触发（见 `CommonEventHandler`）
> - `auratip:showtip_command`：`/showtip` 指令触发（见 `ShowTipCommand`）
>
> 你也可以完全自定义自己的 `type`，关键在于：**你要在某处调用 `TipServer.trigger(type, ...)`**。

### 3) page：页面内容
`page(index, builder -> ...)` 用于创建/更新某个页面。

要求与注意点：

- 至少要有 1 页
- `page_index` 不能重复
- 一页必须至少有一个内容（title/subtitle/content/image 任意一个）

`PageBuilder` 常用方法：

- `title(Component text, float scale, int lineSpacing)`
- `subtitle(Component text, float scale, int lineSpacing)`
- `content(Component text, float scale, int lineSpacing)`
- `titleDivider(...)`：在标题下方加一条分割线
- `image(path, preset, w, h)` / `image(path, x, y, w, h)`
- `imageScaled(...)`：同上，但带 `scale`

图片 `path` 的常见写法：`minecraft:textures/item/apple.png`

### 4) visual：外观与动画
`visual(v -> { ... })` 对应 `TipData.VisualSettings`。

#### 尺寸与位置
- `size(w, h)`：宽高必须 > 0
- `positionPreset(preset)`：预设位置（不区分大小写，内部会转大写）
  - `TOP_LEFT` / `TOP_CENTER` / `TOP_RIGHT`
  - `LEFT_CENTER` / `RIGHT_CENTER`
  - `BOTTOM_LEFT` / `BOTTOM_CENTER` / `BOTTOM_RIGHT`
  - 其他值会按 `CENTER` 处理
- `positionAbsolute(x, y)`：以屏幕 GUI 像素为单位（要求 x/y >= 0）

#### 背景
- `background(type, colors, radius)`：
  - `type=GRADIENT`：建议给 2+ 个颜色
  - `type=SOLID`：建议给 1 个颜色
  - `type=IMAGE`：颜色不一定使用，配合 `backgroundImage(path)`
- `backgroundRounded(true/false)`：是否圆角
- `backgroundImage(path)`：图片背景路径（仅 `IMAGE` 类型使用）

颜色建议用：`#AARRGGBB` 或 `#RRGGBB`（例如 `#CC101010`）。

#### 动画（开/关场）
- `animationStyle(id)`：动画类型 id
- `animationSpeed(speed)`：速度倍率（建议 > 0）
- `animParam(key, value)` / `animParams(map)`：传递动画参数（会转为 `Dynamic`）

内置 transition 动画（`auratip:*`）：

- `auratip:fade_and_slide`（默认，参数：`offset`）
- `auratip:fade`
- `auratip:slide`（参数：`extra_padding`）
- `auratip:slide_in_left`（参数：`extra_padding`）
- `auratip:slide_in_right`（参数：`extra_padding`）
- `auratip:slide_in_top`（参数：`extra_padding`）
- `auratip:slide_in_bottom`（参数：`extra_padding`）

#### 悬停动画（hover）
- `hoverAnimationStyle(id)`
- `hoverAnimationSpeed(speed)`
- `hoverOnlyOnHover(true/false)`：只在鼠标悬停面板时播放
- `hoverParam(...)` / `hoverParams(...)`

内置 hover 动画：

- `auratip:none`（默认）
- `auratip:hover_float`（参数：`amplitude`、`ramp_duration`）
- `auratip:hover_shake`（参数：`amplitude_x`、`amplitude_y`、`frequency_x`、`frequency_y`、`phase`、`ramp_duration`）

#### 主题色与边条
- `themeColor("#55FFFF")`：主题色（影响边条、分割线默认色等）
- `stripeWidth(px)`：左侧竖条宽度
- `stripeLengthFactor(0~1)`：竖条长度比例

### 5) behavior：交互与持续时间
`behavior(b -> { ... })` 对应 `TipData.Behavior`。

- `duration(ticks)`：默认 200 ticks；`-1` 表示一直显示直到关闭
- `pauseOnHover(true/false)`：鼠标悬停时暂停倒计时
- `closeKey("key.keyboard.delete")`：按某个按键关闭（会解析为 `InputConstants.Key`）
- `allowPaging(true/false)`：多页时是否允许左右键翻页

提示：Tip 面板默认也支持：

- 点击右上角 `X` 关闭
- 按 `DEL` 关闭

---

## 变量占位：`${key}` 怎么生效？
AuraTip 只做一件事：在渲染前，把 `Component` 扁平化后，对文字里的 `${...}` 做替换。

你需要：

1. 在 tip 的文字里写 `${key}`
2. 在触发/显示时传入 `Map<String, ?> variables`

变量值可以是：

- `Component`：保留样式
- 任何对象：会走 `toString()` 变成纯文字 `Component.literal(...)`

示例：

```java
TipServer.trigger(myTrigger, player, Map.of(
        "player", player.getDisplayName(),
        "x", player.getBlockX(),
        "z", player.getBlockZ()
));
```

---

## 服务器触发 vs 客户端直接入队

### 服务器触发（推荐）
适用于绝大多数场景：服务器决定“该不该显示”，并负责同步给客户端。

- `TipServer.trigger(type, player, vars)`
- `TipServer.triggerById(tipId, player, vars)`
- `TipServer.show(player, tips, vars)`

### 客户端直接入队（绕开触发规则）
适用于“纯客户端教程/GUI 引导”一类：

- `cc.sighs.auratip.api.client.TipClientApi.enqueue(tips, vars)`（`@OnlyIn(Dist.CLIENT)`）

> 注意：`TipClientApi.enqueue` 不会走 ONCE/冷却，也不会做 trigger.type 匹配。

---

## 自定义动画（Java）
如果内置动画不够用，你可以注册自己的动画 id：

- `cc.sighs.auratip.api.animation.TipAnimations.register(id, factory)`：transition
- `cc.sighs.auratip.api.animation.TipAnimations.registerHover(id, factory)`：hover

注册后就可以在 `visual().animationStyle(...)` / `visual().hoverAnimationStyle(...)` 中使用你的 id。
