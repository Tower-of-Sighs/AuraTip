TipEvents.register(event => {
    event.create('winter_rescue_body_temp')
        .trigger('auratip:first_join_world', 'repeatable', 0)
        .visual(v => {
            v.animationStyle('auratip:fade_and_slide')
            v.animationSpeed(0.6)
            v.animationFrom('BOTTOM_CENTER')
            v.animationTo('BOTTOM_CENTER')
            v.background('solid', ['#CC101010'], 0)
            v.size(185, 115)
            v.position("BOTTOM_CENTER")
            v.hoverAnimationStyle("auratip:hover_shake")
            v.hoverParams({
                "amplitude_x": 3.2,
                "amplitude_y": 2.8,
                "frequency_x": 3.8,
                "frequency_y": 3.1,
                "phase": 0.3,
                "ramp_duration": 0.12
            })

            v.hoverAnimationSpeed(1.2)
        })
        .behavior(b => {
            b.duration(500)
            b.pauseOnHover(true)
            b.closeKey('key.keyboard.delete')
        })
        .page(0, p => {
            const title = TipText.of("体感温度")
                .colorHex("#87CEEB")
                .bold()
                .build();
            p.title(title, 0.85, 0);
            p.titleDivider(1, 2, 4, 1.0, "#87CEEB");

            const content = TipText.join(
                TipText.of("在这个冰天雪地里，保持体温非常重要").colorHex("#87CEEB").build(),
                TipText.of("\n在屏幕下方圆球显示的是你的").colorHex("#87CEEB").build(),
                TipText.of("体感温度").colorHex("#FFA500").build(),
                TipText.of("\n反映你会感到").colorHex("#87CEEB").build(),
                TipText.of("冷").colorHex("#40E0D0").bold().build(),
                TipText.of("还是").colorHex("#87CEEB").build(),
                TipText.of("热").colorHex("#FF8C00").bold().build()
            );

            p.content(content, 0.65, 1);
            p.imageScaled("auratip:textures/gui/img.png", "BOTTOM_CENTER", 253, 95, 0.6);
        });

    event.create('showtip_demo_kjs')
        .trigger('auratip:showtip_command', 'repeatable', 0)
        .visual(v => {
            v.animationStyle('auratip:slide_in_left')
            v.animationSpeed(1.0)
            v.size(200, 55)
            v.position(12, 180)
            v.background('gradient', ['#EE1A1A2E', '#DD12121F'], 6)
            v.backgroundRounded(true)
            v.hoverAnimationStyle('auratip:hover_float')
            v.hoverParam('amplitude', 4.0)
            v.hoverParam('ramp_duration', 0.2)
        })
        .behavior(b => {
            b.duration(160)
            b.pauseOnHover(true)
        })
        .page(0, p => {
            p.title(TipText.of("KJS Tip").colorHex("#55FFFF").bold().build(), 0.8, 0)
            p.content(TipText.of("由 KubeJS 注册，/showtip 会触发我 \n 玩家: ${player}").colorHex("#DDDDDD").build(), 0.65, 1)
        });
});
