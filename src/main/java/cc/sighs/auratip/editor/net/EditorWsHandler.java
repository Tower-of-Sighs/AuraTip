package cc.sighs.auratip.editor.net;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.compat.nekojs.tip.animation.JsHoverAnimation;
import cc.sighs.auratip.compat.nekojs.tip.animation.JsTransitionAnimation;
import cc.sighs.auratip.data.RadialMenuData;
import cc.sighs.auratip.data.TipData;
import cc.sighs.auratip.data.animation.AnimationType;
import cc.sighs.auratip.editor.preview.EditorPreviewApplier;
import cc.sighs.auratip.editor.preview.EditorRadialPreviewApplier;
import cc.sighs.auratip.editor.schema.EditorCodecSchemas;
import com.google.gson.*;
import com.mojang.serialization.JsonOps;
import graal.graalvm.polyglot.Context;
import graal.graalvm.polyglot.Value;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.minecraft.resources.Identifier;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;

final class EditorWsHandler extends SimpleChannelInboundHandler<String> {
    private static final Gson GSON = new Gson();
    private static final Identifier TEMP_TRANSITION_ID = AuraTip.id("editor_temp_transition");
    private static final Identifier TEMP_HOVER_ID = AuraTip.id("editor_temp_hover");

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
                    // Switching editors: close the other preview so only one is visible.
                    EditorPreviewApplier.closePreview();
                    if (lastRadialJson != null) {
                        EditorRadialPreviewApplier.applyMenuJson(lastRadialJson);
                    } else {
                        EditorRadialPreviewApplier.applyDefaultPreview();
                    }
                } else {
                    // Switching editors: close the other preview so only one is visible.
                    EditorRadialPreviewApplier.closePreview();
                    if (lastTipJson != null) {
                        EditorPreviewApplier.applyTipJson(lastTipJson);
                    } else {
                        EditorPreviewApplier.applyDefaultPreview();
                    }
                }
            }
            case "close_preview" -> {
                String which = root.has("mode") ? root.get("mode").getAsString() : "";
                if ("radial".equalsIgnoreCase(which)) {
                    EditorRadialPreviewApplier.closePreview();
                } else if ("tip".equalsIgnoreCase(which)) {
                    EditorPreviewApplier.closePreview();
                } else {
                    // Unknown / not provided: close both previews.
                    EditorPreviewApplier.closePreview();
                    EditorRadialPreviewApplier.closePreview();
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
            case "animation_apply" -> {
                String kind = root.has("kind") ? root.get("kind").getAsString() : "transition";
                String idRaw = root.has("id") ? root.get("id").getAsString() : "";
                JsonElement params = root.has("params") ? root.get("params") : JsonNull.INSTANCE;

                JsonObject result = new JsonObject();
                result.addProperty("type", "animation_apply_result");

                try {
                    Identifier id = normalizeIdOrTemp(kind, idRaw);
                    JsonElement base = lastTipJson != null ? lastTipJson : EditorPreviewCodec.encodeTip(EditorPreviewApplier.defaultTip());

                    if ("hover".equalsIgnoreCase(kind)) {
                        applyStyleOverrideToPreview(base, "hover_animation_style", id.toString(), "hover_animation_params", params);
                    } else {
                        applyStyleOverrideToPreview(base, "animation_style", id.toString(), "animation_params", params);
                    }

                    result.addProperty("ok", true);
                    result.addProperty("id", id.toString());
                } catch (Throwable t) {
                    AuraTip.LOGGER.warn("Editor animation_apply failed", t);
                    result.addProperty("ok", false);
                    result.addProperty("error", String.valueOf(t.getMessage()));
                }

                send(ctx, result);
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
            Identifier id = normalizeIdOrTemp(kind, idRaw);
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

    private record JsEval(Context context, Value value) {
    }

    private static JsEval evalJs(String js) {
        if (js == null || js.isBlank()) {
            throw new IllegalStateException("Empty JS");
        }
        // Editor-side animation prototyping runs in an isolated GraalJS context.
        // We keep the context alive because the returned {@link Value} will be wrapped into
        // {@link JsTransitionAnimation}/{@link JsHoverAnimation} and invoked during preview rendering.
        Context ctx = Context.newBuilder("js")
                .allowAllAccess(true)
                .build();
        Value out = ctx.eval("js", "(" + js + ")");
        return new JsEval(ctx, out);
    }

    private static Value resolveAnimationObject(JsEval eval, Map<String, ?> params) {
        Value value = eval.value();

        // Case 1: JS evaluates to a factory function: (params) => ({ ...methods... })
        if (value != null && value.canExecute()) {
            Value result = value.execute(params);
            if (result != null && (result.hasMembers() || result.canExecute())) {
                return result;
            }
            throw new IllegalStateException("Factory did not return an object");
        }

        // Case 2: JS evaluates to an object with optional create(params) method.
        if (value != null && value.hasMembers()) {
            Value create = value.getMember("create");
            if (create != null && create.canExecute()) {
                Value result = create.execute(params);
                if (result != null && (result.hasMembers() || result.canExecute())) {
                    return result;
                }
                throw new IllegalStateException("create(params) did not return an object");
            }
            return value;
        }

        throw new IllegalStateException("JS did not evaluate to an object or factory function");
    }

    private static Identifier normalizeIdOrTemp(String kind, String raw) {
        String s = raw == null ? "" : raw.trim();
        if (s.isEmpty()) {
            return "hover".equalsIgnoreCase(kind) ? TEMP_HOVER_ID : TEMP_TRANSITION_ID;
        }
        if (s.indexOf(':') < 0) {
            s = "nekojs:" + s;
        }
        Identifier parsed = Identifier.tryParse(s);
        if (parsed == null) {
            throw new IllegalStateException("Invalid Identifier: " + s);
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

    private static JsonArray encodeAnimationIds(Set<Identifier> ids) {
        JsonArray arr = new JsonArray();
        ids.stream()
                .sorted(Comparator.comparing(Identifier::toString))
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

        static Set<Identifier> transition() {
            return AnimationType.listTransitionIds();
        }

        static Set<Identifier> hover() {
            return AnimationType.listHoverIds();
        }
    }

    private static final class EditorPreviewCodec {
        private EditorPreviewCodec() {
        }

        static JsonElement encodeTip(TipData tip) {
            var encoded = TipData.CODEC.encodeStart(JsonOps.INSTANCE, tip);
            return encoded.resultOrPartial(msg -> AuraTip.LOGGER.warn("Editor tip encode error: {}", msg))
                    .orElseGet(() -> GSON.toJsonTree(Map.of()));
        }

        static JsonElement encodeRadial(RadialMenuData menu) {
            var encoded = RadialMenuData.CODEC.encodeStart(JsonOps.INSTANCE, menu);
            return encoded.resultOrPartial(msg -> AuraTip.LOGGER.warn("Editor radial encode error: {}", msg))
                    .orElseGet(() -> GSON.toJsonTree(Map.of()));
        }
    }
}
