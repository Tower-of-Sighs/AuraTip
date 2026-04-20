package cc.sighs.auratip.compat.nekojs.tip.animation;

import cc.sighs.auratip.api.animation.HoverAnimation;
import graal.graalvm.polyglot.Value;

public class JsHoverAnimation implements HoverAnimation {
    private final Value jsObj;

    public JsHoverAnimation(Value jsObj) {
        this.jsObj = jsObj;
    }

    @Override
    public int offsetX(long now, long start, int w, int h, float speed) {
        return callInt("offsetX", now, start, w, h, speed);
    }

    @Override
    public int offsetY(long now, long start, int w, int h, float speed) {
        return callInt("offsetY", now, start, w, h, speed);
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
}
