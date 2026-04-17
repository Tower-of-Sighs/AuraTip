package cc.sighs.auratip.editor.net;

import cc.sighs.auratip.AuraTip;
import cc.sighs.auratip.api.action.ActionHandlers;
import cc.sighs.auratip.data.animation.AnimationType;
import cc.sighs.auratip.util.SerializationUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

final class EditorParamIntrospection {
    private EditorParamIntrospection() {
    }

    static JsonObject buildInitParamPayload() {
        JsonObject out = new JsonObject();
        out.add("animation", buildAnimationParamDefs());
        out.add("actions", buildActionParamDefs());
        return out;
    }

    static JsonObject buildAnimationParamDefs() {
        JsonObject root = new JsonObject();
        root.add("transition", buildTransitionAnimationParams());
        root.add("hover", buildHoverAnimationParams());
        return root;
    }

    static JsonObject buildTransitionAnimationParams() {
        return buildAnimationParams(AnimationType.listTransitionIds(), false);
    }

    static JsonObject buildHoverAnimationParams() {
        return buildAnimationParams(AnimationType.listHoverIds(), true);
    }

    private static JsonObject buildAnimationParams(Set<ResourceLocation> ids, boolean hover) {
        JsonObject out = new JsonObject();
        if (ids == null || ids.isEmpty()) {
            return out;
        }

        ids.stream()
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .forEach(id -> {
                    Map<String, SerializationUtil.CapturedParam> captured = hover
                            ? AnimationType.getDeclaredHoverParams(id)
                            : AnimationType.getDeclaredParams(id);
                    try {
                        if (captured == null || captured.isEmpty()) {
                            captured = SerializationUtil.captureParams(() -> {
                                try {
                                    if (hover) {
                                        AnimationType.resolveHover(id, Map.of());
                                    } else {
                                        AnimationType.resolve(id, Map.of());
                                    }
                                } catch (Throwable ignored) {
                                    // Custom factories (e.g. KubeJS) may require non-empty params or otherwise throw.
                                }
                            });
                        }
                    } catch (Throwable t) {
                        AuraTip.LOGGER.debug("Editor param introspection failed for animation {}", id, t);
                        captured = Map.of();
                    }

                    JsonArray arr = new JsonArray();
                    if (captured != null && !captured.isEmpty()) {
                        captured.entrySet().stream()
                                .sorted(Map.Entry.comparingByKey())
                                .forEach(e -> {
                                    JsonObject p = new JsonObject();
                                    p.addProperty("name", e.getKey());
                                    p.addProperty("kind", e.getValue().kind());
                                    Object fallback = e.getValue().fallback();
                                    if (fallback instanceof Number n) {
                                        p.addProperty("default", n);
                                    } else if (fallback instanceof Boolean b) {
                                        p.addProperty("default", b);
                                    } else if (fallback != null) {
                                        p.addProperty("default", String.valueOf(fallback));
                                    }
                                    arr.add(p);
                                });
                    }

                    out.add(id.toString(), arr);
                });

        return out;
    }

    static JsonObject buildActionParamDefs() {
        JsonObject out = new JsonObject();
        // Built-ins that the editor can render with "structured" inputs.
        out.add("auratip:run_command", params(
                p("command", "string", "")
        ));
        out.add("auratip:simulate_key", params(
                p("key_code", "number", 0)
        ));

        // Tooling schemas for custom script actions (declared at registration time).
        Set<ResourceLocation> types = ActionHandlers.listTypes();
        if (types != null && !types.isEmpty()) {
            types.stream()
                    .sorted(Comparator.comparing(ResourceLocation::toString))
                    .forEach(type -> {
                        Map<String, SerializationUtil.CapturedParam> schema = ActionHandlers.getDeclaredParams(type);
                        if (schema == null || schema.isEmpty()) {
                            return;
                        }
                        JsonArray arr = new JsonArray();
                        schema.entrySet().stream()
                                .sorted(Map.Entry.comparingByKey())
                                .forEach(e -> {
                                    JsonObject p = new JsonObject();
                                    p.addProperty("name", e.getKey());
                                    p.addProperty("kind", e.getValue().kind());
                                    Object fallback = e.getValue().fallback();
                                    if (fallback instanceof Number n) {
                                        p.addProperty("default", n);
                                    } else if (fallback instanceof Boolean b) {
                                        p.addProperty("default", b);
                                    } else if (fallback != null) {
                                        p.addProperty("default", String.valueOf(fallback));
                                    }
                                    arr.add(p);
                                });
                        out.add(type.toString(), arr);
                    });
        }

        // For custom script actions without a schema, the UI should fall back to raw JSON map.
        return out;
    }

    static JsonArray listActionTypes() {
        // Built-ins + registered script handlers (KubeJS Actions.register).
        Set<ResourceLocation> custom = ActionHandlers.listTypes();
        LinkedHashMap<String, Boolean> ordered = new LinkedHashMap<>();
        ordered.put("auratip:run_command", true);
        ordered.put("auratip:simulate_key", true);
        if (custom != null && !custom.isEmpty()) {
            custom.stream()
                    .sorted(Comparator.comparing(ResourceLocation::toString))
                    .forEach(id -> ordered.put(id.toString(), true));
        }

        JsonArray arr = new JsonArray();
        ordered.keySet().forEach(arr::add);
        return arr;
    }

    private static JsonArray params(JsonObject... defs) {
        JsonArray arr = new JsonArray();
        for (JsonObject def : defs) {
            arr.add(def);
        }
        return arr;
    }

    private static JsonObject p(String name, String kind, Object def) {
        JsonObject o = new JsonObject();
        o.addProperty("name", name);
        o.addProperty("kind", kind);
        if (def instanceof Number n) {
            o.addProperty("default", n);
        } else if (def instanceof Boolean b) {
            o.addProperty("default", b);
        } else if (def != null) {
            o.addProperty("default", String.valueOf(def));
        }
        return o;
    }
}
