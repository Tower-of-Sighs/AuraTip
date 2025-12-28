const Minecraft = Java.loadClass('net.minecraft.client.Minecraft');
const InventoryScreen = Java.loadClass('net.minecraft.client.gui.screens.inventory.InventoryScreen');

let ClientQuestFile = null;
try {
    ClientQuestFile = Java.loadClass('dev.ftb.mods.ftbquests.client.ClientQuestFile');
} catch (e) {
    ClientQuestFile = null;
}

Actions.register('open_gui', params => {
    const mc = Minecraft.getInstance();
    if (mc == null || mc.player == null) {
        return;
    }

    const dyn = params.get('screen');
    if (dyn == null) {
        return;
    }
    const screen = dyn.asString('');
    if (!screen) {
        return;
    }

    if (screen === 'inventory_screen') {
        const inv = new InventoryScreen(mc.player);
        mc.setScreen(inv);
        return;
    }

    if (screen === 'ftb_quest_screen') {
        if (ClientQuestFile == null) {
            return;
        }
        if (!ClientQuestFile.exists()) {
            return;
        }
        if (ClientQuestFile.INSTANCE.isDisableGui() && !ClientQuestFile.INSTANCE.canEdit()) {
            return;
        }
        ClientQuestFile.openGui();

    }
});