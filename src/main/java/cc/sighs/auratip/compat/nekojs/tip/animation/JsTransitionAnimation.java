package cc.sighs.auratip.compat.nekojs.tip.animation;

import cc.sighs.auratip.api.animation.TransitionAnimation;
import graal.graalvm.polyglot.Value;

public class JsTransitionAnimation implements TransitionAnimation {
    private final Value jsObj;

    public JsTransitionAnimation(Value jsObj) {
        this.jsObj = jsObj;
    }

    @Override
    public float easedProgress(long now, long start, boolean closing, int openMs, int closeMs) {
        return callFloat("easedProgress", now, start, closing, openMs, closeMs);
    }

    @Override
    public int offsetX(float eased, int w, int h) {
        return callInt("offsetX", eased, w, h);
    }

    @Override
    public int offsetY(float eased, int w, int h) {
        return callInt("offsetY", eased, w, h);
    }

    private int callInt(String name, Object... args) {
        Value fn = jsObj.getMember(name);
        if (fn == null || !fn.canExecute()) {
            return 0;
        }
        Value result = fn.execute(args);
        if (result == null) {
            return 0;
        }
        if (result.fitsInInt()) {
            return result.asInt();
        }
        if (result.fitsInLong()) {
            return (int) result.asLong();
        }
        if (result.fitsInDouble()) {
            return (int) result.asDouble();
        }
        return 0;
    }

    private float callFloat(String name, Object... args) {
        Value fn = jsObj.getMember(name);
        if (fn == null || !fn.canExecute()) {
            return 0.0f;
        }
        Value result = fn.execute(args);
        if (result == null) {
            return 0.0f;
        }
        if (result.fitsInFloat()) {
            return result.asFloat();
        }
        if (result.fitsInDouble()) {
            return (float) result.asDouble();
        }
        if (result.fitsInLong()) {
            return (float) result.asLong();
        }
        return 0.0f;
    }
}
