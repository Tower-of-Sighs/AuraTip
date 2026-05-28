package cc.sighs.auratip.api.radiamenu.icon;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A renderable icon for a radial menu slot.
 * <p>
 * Built-in implementations: {@link TextureIcon} (texture blit) and {@link ItemIcon} (full item model).
 * Custom implementations can be registered via {@link #register(Identifier, Class, Codec)}.
 */
public interface IRadialIcon {

    /**
     * Renders this icon at the given position.
     *
     * @param graphics the current GuiGraphics
     * @param x        center x position
     * @param y        center y position
     * @param scale    size multiplier (base 24px for textures, 16px for items)
     * @param alpha    opacity factor (0..1), driven by menu open/close animation
     */
    void render(GuiGraphicsExtractor graphics, int x, int y, float scale, float alpha);

    /**
     * Returns the codec used to serialize and deserialize this icon type.
     */
    Codec<? extends IRadialIcon> codec();

    // ---- type registry ----

    Map<Identifier, Codec<? extends IRadialIcon>> BY_ID = new ConcurrentHashMap<>();
    Map<Class<?>, Identifier> ID_BY_CLASS = new ConcurrentHashMap<>();

    /**
     * Registers a custom icon codec for data pack support.
     *
     * @param type  unique type identifier
     * @param clazz the icon implementation class
     * @param codec codec for this icon type
     */
    static <T extends IRadialIcon> void register(Identifier type, Class<T> clazz, Codec<T> codec) {
        BY_ID.put(type, codec);
        ID_BY_CLASS.put(clazz, type);
    }

    /**
     * Returns the type id for this icon, used for codec dispatch.
     */
    default Identifier type() {
        Identifier id = ID_BY_CLASS.get(getClass());
        if (id != null) return id;
        throw new IllegalStateException("Unregistered IRadialIcon: " + getClass().getName());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    Codec<IRadialIcon> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<Pair<IRadialIcon, T>> decode(DynamicOps<T> ops, T input) {
            var stringResult = ops.getStringValue(input);
            if (stringResult.result().isPresent()) {
                return Identifier.CODEC.decode(ops, input)
                        .map(p -> p.mapFirst(loc -> new TextureIcon(loc, 1.0f)));
            }
            var typeVal = ops.get(input, "type");
            if (typeVal.result().isEmpty()) {
                return DataResult.error(() -> "Cannot decode IRadialIcon: expected a Identifier string or an object with a 'type' field");
            }
            var typeResult = ops.getStringValue(typeVal.result().get());
            if (typeResult.result().isEmpty()) {
                return DataResult.error(() -> "IRadialIcon 'type' field must be a string");
            }
            String typeStr = typeResult.result().get();
            Identifier typeId = Identifier.tryParse(typeStr);
            if (typeId == null) {
                return DataResult.error(() -> "Invalid IRadialIcon type: '" + typeStr + "'. Must be a Identifier like 'modid:path'.");
            }
            Codec<? extends IRadialIcon> codec = BY_ID.get(typeId);
            if (codec == null) {
                return DataResult.error(() -> "Unknown IRadialIcon type: '" + typeStr + "'. Registered types: " + BY_ID.keySet());
            }
            return ((Codec) codec).decode(ops, input);
        }

        @Override
        public <T> DataResult<T> encode(IRadialIcon input, DynamicOps<T> ops, T prefix) {
            if (input instanceof TextureIcon ti) {
                if (ti.scale() == 1.0f) {
                    return Identifier.CODEC.encode(ti.id(), ops, prefix);
                }
                return ((Codec) TextureIcon.CODEC).encode(ti, ops, prefix);
            }
            Identifier typeId = input.type();
            Codec<? extends IRadialIcon> codec = BY_ID.get(typeId);
            if (codec == null) {
                return DataResult.error(() -> "Unregistered IRadialIcon type: " + typeId);
            }
            return ((Codec) codec).encode(input, ops, prefix);
        }
    };
}
