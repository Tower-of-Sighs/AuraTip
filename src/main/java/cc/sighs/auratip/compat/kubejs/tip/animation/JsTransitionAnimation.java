package cc.sighs.auratip.compat.kubejs.tip.animation;

import cc.sighs.auratip.data.animation.ta.TransitionAnimation;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.Function;
import dev.latvian.mods.rhino.Scriptable;

public class JsTransitionAnimation implements TransitionAnimation {

    private final Scriptable jsObj;

    public JsTransitionAnimation(Scriptable jsObj) {
        this.jsObj = jsObj;
    }

    @Override
    public float easedProgress(long now, long start, boolean closing, int openMs, int closeMs) {
        return callFloat(Context.enter(), "easedProgress", now, start, closing, openMs, closeMs);
    }

    @Override
    public int offsetX(float eased, int w, int h) {
        return callInt(Context.enter(), "offsetX", eased, w, h);
    }

    @Override
    public int offsetY(float eased, int w, int h) {
        return callInt(Context.enter(), "offsetY", eased, w, h);
    }

    private int callInt(Context context, String name, Object... args) {
        Object fn = jsObj.get(context, name, jsObj);
        if (!(fn instanceof Function f)) return 0;
        Object result = f.call(context, jsObj.getParentScope(), jsObj, args);
        return result instanceof Number n ? n.intValue() : 0;
    }

    private float callFloat(Context context, String name, Object... args) {
        Object fn = jsObj.get(context, name, jsObj);
        if (!(fn instanceof Function f)) return 0.0f;
        Object result = f.call(context, jsObj.getParentScope(), jsObj, args);
        return result instanceof Number n ? n.floatValue() : 0.0f;
    }
}