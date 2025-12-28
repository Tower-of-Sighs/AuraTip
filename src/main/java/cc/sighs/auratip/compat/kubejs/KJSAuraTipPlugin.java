package cc.sighs.auratip.compat.kubejs;

import cc.sighs.auratip.compat.kubejs.radiamenu.RadialMenuRegistrationEvent;
import cc.sighs.auratip.compat.kubejs.radiamenu.RadialMenuScriptRegistry;
import cc.sighs.auratip.compat.kubejs.radiamenu.action.ActionsKJS;
import cc.sighs.auratip.compat.kubejs.radiamenu.slot.RadialMenuExtraSlotRegistry;
import cc.sighs.auratip.compat.kubejs.radiamenu.slot.RadialMenusKJS;
import cc.sighs.auratip.compat.kubejs.tip.*;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.event.EventGroup;
import dev.latvian.mods.kubejs.event.EventHandler;
import dev.latvian.mods.kubejs.script.BindingsEvent;

public class KJSAuraTipPlugin extends KubeJSPlugin {
    public static final EventGroup TIP_EVENTS = EventGroup.of("TipEvents");
    public static final EventGroup RADIAL_EVENTS = EventGroup.of("RadialMenuEvents");

    public static final EventHandler REGISTER_TIPS = TIP_EVENTS.server("register",
            () -> TipRegistrationEvent.class);
    public static final EventHandler REGISTER_RADIAL = RADIAL_EVENTS.server("register",
            () -> RadialMenuRegistrationEvent.class);

    @Override
    public void registerEvents() {
        TIP_EVENTS.register();
        RADIAL_EVENTS.register();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("TipVars", TipVariables.class);
        event.add("TipTriggers", TipTriggers.class);
        event.add("TipText", TipText.class);
        event.add("TipAnimations", TipAnimations.class);
        event.add("Actions", ActionsKJS.class);
        event.add("RadialMenus", RadialMenusKJS.class);
    }

    @Override
    public void onServerReload() {
        RadialMenuExtraSlotRegistry.clear();

        TipRegistrationEvent tipEvent = new TipRegistrationEvent();
        REGISTER_TIPS.post(tipEvent);
        var tips = tipEvent.buildAll();
        TipScriptRegistry.setTips(tipEvent.buildAll());

        RadialMenuRegistrationEvent radialEvent = new RadialMenuRegistrationEvent();
        REGISTER_RADIAL.post(radialEvent);
        var menus = radialEvent.buildAll();
        RadialMenuScriptRegistry.setMenus(menus);
    }
}
