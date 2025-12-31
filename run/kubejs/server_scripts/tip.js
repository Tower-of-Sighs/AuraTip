TipEvents.register(event => {
    event.create('winter_rescue_body_temp')
        .trigger('FIRST_JOIN_WORLD', 'repeatable', 0)
        .visual(v => {
            v.animationStyle('kjs_bounce_left')
            v.animParams({
                "overshoot": 2.4,
                "extra_distance": 80
            })
            // v.animationStyle('fade_and_slide')
            v.animationSpeed(0.6)
            v.animationFrom('BOTTOM_CENTER')
            v.animationTo('BOTTOM_CENTER')
            v.background('solid', ['#CC101010'], 0)
            v.size(185, 115)
            v.position("BOTTOM_CENTER")
            v.hoverAnimationStyle("hover_shake")
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
});