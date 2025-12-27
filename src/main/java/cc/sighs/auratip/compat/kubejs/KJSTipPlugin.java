package cc.sighs.auratip.compat.kubejs;

import cc.sighs.auratip.AuraTip;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.script.BindingsEvent;

public class KJSTipPlugin extends KubeJSPlugin {
    public static final EventGroup TIP_EVENTS = EventGroup.of("TipEvents");

    public static final EventHandler REGISTER_TIPS = TIP_EVENTS.server("register",
            () -> TipRegistrationEvent.class);

    @Override
    public void registerEvents() {
        TIP_EVENTS.register();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("TipVars", TipVariables.class);
        event.add("TipTriggers", TipTriggers.class);
    }

    @Override
    public void onServerReload() {
        TipRegistrationEvent event = new TipRegistrationEvent();
        REGISTER_TIPS.post(event);
        var tips = event.buildAll();
        AuraTip.LOGGER.info("[AuraTip] Loaded {} script tips", tips.size());
        TipScriptRegistry.setTips(event.buildAll());
    }
}
