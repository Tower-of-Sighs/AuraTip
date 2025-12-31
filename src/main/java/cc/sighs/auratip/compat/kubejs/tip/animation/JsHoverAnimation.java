package cc.sighs.auratip.compat.kubejs.tip.animation;

import cc.sighs.auratip.data.animation.ha.HoverAnimation;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Function;
import dev.latvian.mods.rhino.Scriptable;

public class JsHoverAnimation implements HoverAnimation {

    private final Scriptable jsObj;

    public JsHoverAnimation(Scriptable jsObj) {
        this.jsObj = jsObj;
    }

    @Override
    public int offsetX(long now, long start, int w, int h, float speed) {
        return callInt(Context.enter(), "offsetX", now, start, w, h, speed);
    }

    @Override
    public int offsetY(long now, long start, int w, int h, float speed) {
        return callInt(Context.enter(),"offsetY", now, start, w, h, speed);
    }

    private int callInt(Context context, String name, Object... args) {
        Object fn = jsObj.get(context, name, jsObj);
        if (!(fn instanceof Function f)) return 0;

        Object result = f.call(
                context,
                jsObj,
                jsObj,
                args
        );
        return result instanceof Number n ? n.intValue() : 0;
    }
}
