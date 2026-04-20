package cc.sighs.auratip.compat.nekojs;

import cc.sighs.auratip.api.action.ActionHandlers;
import cc.sighs.auratip.compat.nekojs.radiamenu.RadialMenuRegistrationEvent;
import cc.sighs.auratip.compat.nekojs.radiamenu.RadialMenuScriptRegistry;
import cc.sighs.auratip.compat.nekojs.radiamenu.action.ActionsNJS;
import cc.sighs.auratip.compat.nekojs.radiamenu.slot.RadialMenuExtraSlotRegistry;
import cc.sighs.auratip.compat.nekojs.radiamenu.slot.RadialMenusNJS;
import cc.sighs.auratip.compat.nekojs.tip.*;
import cc.sighs.auratip.compat.nekojs.tip.animation.TipAnimations;
import cc.sighs.auratip.data.animation.AnimationType;
import com.tkisor.nekojs.NekoJS;
import com.tkisor.nekojs.api.NekoJSPlugin;
import com.tkisor.nekojs.api.annotation.RegisterNekoJSPlugin;
import com.tkisor.nekojs.api.data.Binding;
import com.tkisor.nekojs.api.data.BindingsRegister;
import com.tkisor.nekojs.api.event.EventBusJS;
import com.tkisor.nekojs.api.event.EventGroup;
import com.tkisor.nekojs.api.event.EventGroupRegistry;

import java.util.List;

@RegisterNekoJSPlugin
public class NJSAuraTipPlugin implements NekoJSPlugin {
    public static final EventGroup TIP_EVENTS = EventGroup.of("TipEvents");
    public static final EventGroup RADIAL_EVENTS = EventGroup.of("RadialMenuEvents");

    public static final EventBusJS<TipRegistrationEvent, Void> REGISTER_TIPS_CLIENT = TIP_EVENTS.client("register", TipRegistrationEvent.class);
    public static final EventBusJS<RadialMenuRegistrationEvent, Void> REGISTER_RADIAL = RADIAL_EVENTS.client("register", RadialMenuRegistrationEvent.class);

    @Override
    public void registerClientEvents(EventGroupRegistry registry) {
        registry.register(TIP_EVENTS);
        registry.register(RADIAL_EVENTS);
    }

    @Override
    public void registerBindings(BindingsRegister registry) {
        registry.register(Binding.of("TipVars", TipVariables.class));
        registry.register(Binding.of("TipTriggers", TipTriggers.class));
        registry.register(Binding.of("TipText", TipText.class));
        registry.register(Binding.of("TipAnimations", TipAnimations.class));
        registry.register(Binding.of("Actions", ActionsNJS.class));
        registry.register(Binding.of("RadialMenus", RadialMenusNJS.class));
    }
    
    public static void refreshClientRegistries() {
        RadialMenuExtraSlotRegistry.clear();

        TipRegistrationEvent tipEvent = new TipRegistrationEvent();
        REGISTER_TIPS_CLIENT.post(tipEvent);
        TipScriptRegistry.setTips(tipEvent.buildAll());

        RadialMenuRegistrationEvent radialEvent = new RadialMenuRegistrationEvent();
        REGISTER_RADIAL.post(radialEvent);
        RadialMenuScriptRegistry.setMenus(radialEvent.buildAll());
    }


    public static void clearClientRegistries() {
        TipScriptRegistry.setTips(List.of());
        RadialMenuScriptRegistry.setMenus(List.of());
        RadialMenuExtraSlotRegistry.clear();
        
        for (var type : ActionHandlers.listTypes()) {
            if (NekoJS.MODID.equals(type.getNamespace())) {
                ActionHandlers.clear(type);
            }
        }

        for (var id : AnimationType.listTransitionIds()) {
            if (NekoJS.MODID.equals(id.getNamespace())) {
                AnimationType.clearTransitionInternal(id);
            }
        }
        for (var id : AnimationType.listHoverIds()) {
            if (NekoJS.MODID.equals(id.getNamespace())) {
                AnimationType.clearHoverInternal(id);
            }
        }
    }
}
