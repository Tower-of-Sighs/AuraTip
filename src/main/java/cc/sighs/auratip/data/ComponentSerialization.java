package cc.sighs.auratip.data;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import net.minecraft.network.chat.Component;

public class ComponentSerialization {
    public static final Codec<Component> COMPONENT_CODEC = Codec.PASSTHROUGH.xmap(
            dynamic -> {
                JsonElement element = dynamic.convert(JsonOps.INSTANCE).getValue();
                return Component.Serializer.fromJson(element);
            },
            component -> new Dynamic<>(JsonOps.INSTANCE, Component.Serializer.toJsonTree(component))
    );
}
