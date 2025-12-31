package cc.sighs.auratip.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public final class ResolveUtil {

    private ResolveUtil() {
    }

    public static @NotNull Component resolveVariables(@Nullable Component component, @Nullable Map<String, ?> variables) {
        if (component == null || variables == null || variables.isEmpty()) {
            return Objects.requireNonNullElse(component, Component.empty());
        }
        Map<String, Component> componentVars = new HashMap<>();
        for (Map.Entry<String, ?> entry : variables.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            if (key == null || key.isEmpty() || value == null) {
                continue;
            }
            if (value instanceof Component valueComponent) {
                componentVars.put(key, valueComponent);
            } else {
                componentVars.put(key, Component.literal(value.toString()));
            }
        }
        if (componentVars.isEmpty()) {
            return component;
        }
        return resolveVariablesInternal(component, componentVars);
    }

    private static @NotNull Component resolveVariablesInternal(Component component, Map<String, Component> variables) {
        List<Component> flat = component.toFlatList();
        boolean hasPlaceholder = false;
        for (Component part : flat) {
            String text = part.getString();
            if (text.contains("${")) {
                hasPlaceholder = true;
                break;
            }
        }
        if (!hasPlaceholder) {
            return component;
        }
        List<Component> resultParts = new ArrayList<>();
        for (Component part : flat) {
            Style style = part.getStyle();
            String text = part.getString();
            if (!text.contains("${")) {
                if (!text.isEmpty()) {
                    resultParts.add(Component.literal(text).withStyle(style));
                }
                continue;
            }
            resultParts.addAll(resolveInLiteral(text, style, variables, 0));
        }
        if (resultParts.isEmpty()) {
            return Component.empty();
        }
        MutableComponent result = Component.empty();
        for (Component part : resultParts) {
            result.append(part);
        }
        return result;
    }

    private static List<Component> resolveInLiteral(String text, Style baseStyle, Map<String, Component> variables, int depth) {
        List<Component> parts = new ArrayList<>();
        if (text.isEmpty()) {
            return parts;
        }
        StringBuilder buffer = new StringBuilder();
        int length = text.length();
        int i = 0;
        while (i < length) {
            char c = text.charAt(i);
            if (c == '$' && i + 1 < length && text.charAt(i + 1) == '{') {
                int endIndex = text.indexOf('}', i + 2);
                if (endIndex > i + 2) {
                    if (!buffer.isEmpty()) {
                        parts.add(Component.literal(buffer.toString()).withStyle(baseStyle));
                        buffer.setLength(0);
                    }
                    String key = text.substring(i + 2, endIndex);
                    Component valueComponent = variables.get(key);
                    if (valueComponent != null) {
                        Component resolvedValue = depth < 4 ? resolveVariablesInternal(valueComponent, variables) : valueComponent;
                        List<Component> valueParts = resolvedValue.toFlatList(baseStyle);
                        parts.addAll(valueParts);
                    }
                    i = endIndex + 1;
                    continue;
                }
            }
            buffer.append(c);
            i++;
        }
        if (!buffer.isEmpty()) {
            parts.add(Component.literal(buffer.toString()).withStyle(baseStyle));
        }
        return parts;
    }
}
