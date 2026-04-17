package cc.sighs.auratip.editor.net;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.compat.kubejs.tip.animation.JsHoverAnimation;
import cc.sighs.auratip.compat.kubejs.tip.animation.JsTransitionAnimation;
import cc.sighs.auratip.data.RadialMenuData;
import cc.sighs.auratip.data.animation.AnimationType;
import cc.sighs.auratip.editor.preview.EditorPreviewApplier;
import cc.sighs.auratip.editor.preview.EditorRadialPreviewApplier;
import cc.sighs.auratip.editor.schema.EditorCodecSchemas;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.latvian.mods.rhino.Context;
import dev.latvian.mods.rhino.ContextFactory;
import dev.latvian.mods.rhino.Scriptable;
import dev.latvian.mods.rhino.Function;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.resources.ResourceLocation;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;

final class EditorWsHandler extends SimpleChannelInboundHandler<String> {
    private static final Gson GSON = new Gson();
    private static final ContextFactory CONTEXT_FACTORY = new ContextFactory();
    private static final ResourceLocation TEMP_TRANSITION_ID = AuraTip.id("editor_temp_transition");
    private static final ResourceLocation TEMP_HOVER_ID = AuraTip.id("editor_temp_hover");

    private final EditorWsHub hub;

    private volatile JsonElement lastTipJson;
    private volatile JsonElement lastRadialJson;
    private volatile String mode = "tip";

    EditorWsHandler(EditorWsHub hub) {
        this.hub = hub;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        hub.add(ctx.channel());
        sendInit(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) {
        hub.remove(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String text) {
        JsonObject root;
        try {
            JsonElement parsed = JsonParser.parseString(text);
            if (!parsed.isJsonObject()) {
                return;
            }
            root = parsed.getAsJsonObject();
        } catch (Exception e) {
            AuraTip.LOGGER.warn("Editor WS: invalid json: {}", text, e);
            return;
        }

        String type = root.has("type") ? root.get("type").getAsString() : "";
        switch (type) {
            case "ping" -> send(ctx, json("type", "pong"));
            case "set_mode" -> {
                String next = root.has("mode") ? root.get("mode").getAsString() : "tip";
                mode = next == null ? "tip" : next;
                if ("radial".equalsIgnoreCase(mode)) {
                    if (lastRadialJson != null) {
                        EditorRadialPreviewApplier.applyMenuJson(lastRadialJson);
                    } else {
                        EditorRadialPreviewApplier.applyDefaultPreview();
                    }
                } else {
                    if (lastTipJson != null) {
                        EditorPreviewApplier.applyTipJson(lastTipJson);
                    } else {
                        EditorPreviewApplier.applyDefaultPreview();
                    }
                }
            }
            case "tip_update" -> {
                JsonElement tip = root.get("tip");
                if (tip != null) {
                    lastTipJson = tip;
                    if (!"radial".equalsIgnoreCase(mode)) {
                        EditorPreviewApplier.applyTipJson(tip);
                    }
                }
            }
            case "radial_update" -> {
                JsonElement menu = root.get("menu");
                if (menu != null) {
                    lastRadialJson = menu;
                    if ("radial".equalsIgnoreCase(mode)) {
                        EditorRadialPreviewApplier.applyMenuJson(menu);
                    }
                }
            }
            case "animation_test" -> {
                String kind = root.has("kind") ? root.get("kind").getAsString() : "transition";
                String id = root.has("id") ? root.get("id").getAsString() : "";
                String js = root.has("js") ? root.get("js").getAsString() : "";
                JsonElement params = root.has("params") ? root.get("params") : JsonNull.INSTANCE;
                handleAnimationTest(ctx, kind, id, js, params);
            }
            default -> {
            }
        }
    }

    private static void sendInit(ChannelHandlerContext ctx) {
        JsonObject init = new JsonObject();
        init.addProperty("type", "init");

        JsonObject payload = new JsonObject();
        payload.add("defaultTip", EditorPreviewCodec.encodeTip(EditorPreviewApplier.defaultTip()));
        payload.add("defaultRadialMenu", EditorPreviewCodec.encodeRadial(EditorRadialPreviewApplier.defaultMenu()));
        payload.add("transitionAnimations", encodeAnimationIds(AnimationTypeIds.transition()));
        payload.add("hoverAnimations", encodeAnimationIds(AnimationTypeIds.hover()));
        payload.add("paramMeta", EditorParamIntrospection.buildInitParamPayload());
        payload.add("actionTypes", EditorParamIntrospection.listActionTypes());
        payload.add("schemas", EditorCodecSchemas.buildAll());

        init.add("payload", payload);
        send(ctx, init);
    }

    private void handleAnimationTest(ChannelHandlerContext ctx, String kind, String idRaw, String js, JsonElement paramsJson) {
        JsonObject result = new JsonObject();
        result.addProperty("type", "animation_test_result");
        result.addProperty("kind", kind == null ? "" : kind);

        try {
            ResourceLocation id = normalizeIdOrTemp(kind, idRaw);
            JsEval eval = evalJs(js);

            if ("hover".equalsIgnoreCase(kind)) {
                AnimationType.registerOrReplaceHoverInternal(id, params -> new JsHoverAnimation(resolveAnimationObject(eval, params)));
                result.addProperty("id", id.toString());
                applyStyleOverrideToPreview(lastTipJson, "hover_animation_style", id.toString(), "hover_animation_params", paramsJson);
            } else {
                AnimationType.registerOrReplaceInternal(id, params -> new JsTransitionAnimation(resolveAnimationObject(eval, params)));
                result.addProperty("id", id.toString());
                applyStyleOverrideToPreview(lastTipJson, "animation_style", id.toString(), "animation_params", paramsJson);
            }

            result.addProperty("ok", true);
        } catch (Throwable t) {
            AuraTip.LOGGER.warn("Editor animation_test failed", t);
            result.addProperty("ok", false);
            result.addProperty("error", String.valueOf(t.getMessage()));
        }

        send(ctx, result);
    }

    private record JsEval(Scriptable scope, Object value) {
    }

    private static JsEval evalJs(String js) {
        if (js == null || js.isBlank()) {
            throw new IllegalStateException("Empty JS");
        }
        Context cx = CONTEXT_FACTORY.enter();
        Scriptable scope = cx.initStandardObjects();
        Object out = cx.evaluateString(scope, "(" + js + ")", "auratip_editor_anim", 1, null);
        return new JsEval(scope, out);
    }

    private static Scriptable resolveAnimationObject(JsEval eval, Map<String, ?> params) {
        Context cx = CONTEXT_FACTORY.enter();
        Object value = eval.value();
        if (value instanceof Function factory) {
            return callFactory(cx, eval.scope(), factory, eval.scope(), params);
        }
        if (value instanceof Scriptable scriptable) {
            Object create = scriptable.get(cx, "create", scriptable);
            if (create instanceof Function f) {
                return callFactory(cx, eval.scope(), f, scriptable, params);
            }
            return scriptable;
        }
        throw new IllegalStateException("JS did not evaluate to an object or factory function");
    }

    private static Scriptable callFactory(Context cx, Scriptable scope, Function fn, Scriptable thisObj, Map<String, ?> params) {
        Object result = fn.call(cx, scope, thisObj, new Object[]{params});
        if (result instanceof Scriptable s) {
            return s;
        }
        throw new IllegalStateException("Factory did not return an object");
    }

    private static ResourceLocation normalizeIdOrTemp(String kind, String raw) {
        String s = raw == null ? "" : raw.trim();
        if (s.isEmpty()) {
            return "hover".equalsIgnoreCase(kind) ? TEMP_HOVER_ID : TEMP_TRANSITION_ID;
        }
        if (s.indexOf(':') < 0) {
            s = "kubejs:" + s;
        }
        ResourceLocation parsed = ResourceLocation.tryParse(s);
        if (parsed == null) {
            throw new IllegalStateException("Invalid ResourceLocation: " + s);
        }
        return parsed;
    }

    private static void applyStyleOverrideToPreview(JsonElement lastTipJson, String styleKey, String styleValue, String paramsKey, JsonElement paramsValue) {
        if (lastTipJson == null || !lastTipJson.isJsonObject()) {
            return;
        }

        JsonObject copy = lastTipJson.getAsJsonObject().deepCopy();
        JsonObject visual = copy.has("visual_settings") && copy.get("visual_settings").isJsonObject()
                ? copy.getAsJsonObject("visual_settings")
                : new JsonObject();

        visual.addProperty(styleKey, styleValue);
        if (paramsKey != null && paramsValue != null && paramsValue.isJsonObject()) {
            visual.add(paramsKey, paramsValue.getAsJsonObject().deepCopy());
        }
        copy.add("visual_settings", visual);

        EditorPreviewApplier.applyTipJson(copy);
    }

    private static JsonArray encodeAnimationIds(Set<ResourceLocation> ids) {
        JsonArray arr = new JsonArray();
        ids.stream()
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .forEach(id -> arr.add(id.toString()));
        return arr;
    }

    private static void send(ChannelHandlerContext ctx, JsonObject obj) {
        ctx.writeAndFlush(GSON.toJson(obj));
    }

    private static JsonObject json(String k, String v) {
        JsonObject o = new JsonObject();
        o.addProperty(k, v);
        return o;
    }

    /**
     * Small adapter so we can list ids without exposing factories.
     */
    private static final class AnimationTypeIds {
        private AnimationTypeIds() {
        }

        static Set<ResourceLocation> transition() {
            return AnimationType.listTransitionIds();
        }

        static Set<ResourceLocation> hover() {
            return AnimationType.listHoverIds();
        }
    }

    private static final class EditorPreviewCodec {
        private EditorPreviewCodec() {
        }

        static JsonElement encodeTip(cc.sighs.auratip.data.TipData tip) {
            var encoded = cc.sighs.auratip.data.TipData.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, tip);
            return encoded.resultOrPartial(msg -> AuraTip.LOGGER.warn("Editor tip encode error: {}", msg))
                    .orElseGet(() -> GSON.toJsonTree(Map.of()));
        }

        static JsonElement encodeRadial(RadialMenuData menu) {
            var encoded = RadialMenuData.CODEC.encodeStart(com.mojang.serialization.JsonOps.INSTANCE, menu);
            return encoded.resultOrPartial(msg -> AuraTip.LOGGER.warn("Editor radial encode error: {}", msg))
                    .orElseGet(() -> GSON.toJsonTree(Map.of()));
        }
    }
}
