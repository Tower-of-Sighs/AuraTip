RadialMenuEvents.register(event => {
    const invAction = Actions.of('open_gui', {screen: 'inventory_screen'});
    const ftbAction = Actions.of('open_gui', {screen: 'ftb_quest_screen'});

    RadialMenus.addSlot(
        'auratip:example_menu',
        'Inventory',
        'minecraft:textures/item/apple.png',
        invAction,
        TipText.translatable('tip.auratip.open_inventory').colorHex("#55FFFF").bold().underlined().build(),
        '#EEEEEE'
    );

    RadialMenus.addSlot(
        'auratip:example_menu',
        'FTB Quests',
        'ftbquests:textures/item/book.png',
        ftbAction,
        TipText.translatable('tip.auratip.open_quests').colorHex("#55FFFF").bold().underlined().build(),
        '#FFD700'
    );

    const menu = event.create('demo_menu');

    menu.radii(55, 100);
    menu.ringColors(['#1A0B1526', '#D92B5A9E']);

    menu.slot(
        'ShowTip',
        'minecraft:textures/item/paper.png',
        Actions.of('run_command', {command: '/showtip'}),
        TipText.of('/showtip').colorHex("#55FFFF").bold().underlined().build(),
        '#77FFFFFF'
    );

    menu.slot(
        'Java Action',
        'minecraft:textures/item/diamond.png',
        Actions.of('auratip:dev_action', {message: 'Hello from KubeJS menu'}),
        TipText.of('auratip:dev_action').colorHex("#55FFFF").bold().underlined().build(),
        '#77FFFFFF'
    );

    // Validation slots:
    // - server_probe handler is registered from server_scripts (see auratip_action_probe.js)

    menu.slot(
        'Server Probe',
        'minecraft:textures/item/redstone.png',
        Actions.of('server_probe'),
        TipText.of('server_probe (server_scripts)').colorHex("#AAAAAA").build(),
        '#77FFFFFF'
    );

    menu.slot(
        'Server Logic Test',
        'minecraft:textures/item/book.png',
        Actions.of('run_command', {command: '/help'}),
        TipText.of('run_command: /help').colorHex("#AAAAAA").build(),
        '#77FFFFFF'
    );
});
