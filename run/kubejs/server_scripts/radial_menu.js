RadialMenuEvents.register(event => {
    const invAction = Actions.of('open_gui', {screen: 'inventory_screen'});
    // const ftbAction = Actions.of('open_gui', {screen: 'ftb_quest_screen'});

    RadialMenus.addSlot(
        'Inventory',
        'minecraft:textures/item/apple.png',
        invAction,
        TipText.translatable('tip.auratip.open_inventory').colorHex("#55FFFF").bold().underlined().build(),
        '#EEEEEE'
    );

    // RadialMenus.addSlot(
    //     'FTB Quests',
    //     'ftbquests:textures/item/book.png',
    //     ftbAction,
    //     TipText.translatable('tip.auratip.open_quests').colorHex("#55FFFF").bold().underlined().build(),
    //     '#FFD700'
    // );
});
