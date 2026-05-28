package cc.sighs.auratip.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * Utility helpers for building {@link Codec} instances, reducing repetitive
 * {@code Codec.either} patterns.
 */
public final class CodecUtil {

    private CodecUtil() {}

    /**
     * Accepts either a plain string or an int list.
     * Used by {@link cc.sighs.auratip.data.TipData.Position}.
     */
    public static <T> Codec<T> stringOrIntList(
            Function<String, T> fromString,
            Function<List<Integer>, T> fromList,
            Function<T, String> toString,
            Function<T, List<Integer>> toList) {
        return Codec.either(
                Codec.STRING,
                Codec.INT.listOf()
        ).xmap(
                either -> either.map(fromString, fromList),
                value -> {
                    String s = toString.apply(value);
                    if (s != null) return Either.left(s);
                    return Either.right(toList.apply(value));
                }
        );
    }

    /**
     * Accepts either a single int (uniform) or an int list.
     */
    public static <T> Codec<T> intOrList(
            Function<Integer, T> fromInt,
            Function<List<Integer>, T> fromList,
            Function<T, Integer> toInt,
            Function<T, List<Integer>> toList) {
        return Codec.either(
                Codec.INT,
                Codec.INT.listOf()
        ).xmap(
                either -> either.map(fromInt, fromList),
                value -> {
                    int v = toInt.apply(value);
                    if (v != 0) return Either.left(v);
                    return Either.right(toList.apply(value));
                }
        );
    }

    /**
     * Accepts either an object with named fields, a list, or a single int.
     * Used by {@link cc.sighs.auratip.data.TipData.Padding}.
     */
    public static <T> Codec<T> intOrListOrObject(
            Function<Integer, T> fromInt,
            Function<List<Integer>, T> fromList,
            Function<T, Integer> toInt,
            Function<T, List<Integer>> toList,
            Codec<T> objectCodec) {
        return Codec.either(
                Codec.INT,
                Codec.either(
                        Codec.INT.listOf(),
                        objectCodec
                )
        ).xmap(
                either -> {
                    if (either.left().isPresent()) return fromInt.apply(either.left().get());
                    var inner = either.right().get();
                    return inner.map(fromList, Function.identity());
                },
                value -> {
                    int v = toInt.apply(value);
                    if (v != 0) return Either.left(v);
                    return Either.right(Either.left(toList.apply(value)));
                }
        );
    }

    /**
     * Standard case-insensitive enum codec.
     * <p>Decodes {@code "once"} → {@code Mode.ONCE}; encodes back to lowercase.</p>
     */
    public static <E extends Enum<E>> Codec<E> enumCodec(Class<E> enumClass) {
        return Codec.STRING.xmap(
                value -> {
                    for (E c : enumClass.getEnumConstants()) {
                        if (c.name().equalsIgnoreCase(value)) return c;
                    }
                    return Enum.valueOf(enumClass, value.toUpperCase(Locale.ROOT));
                },
                e -> e.name().toLowerCase(Locale.ROOT)
        );
    }

    /**
     * Parses an ARGB value that may be a hex string ({@code "#CC000000"})
     * or an integer ({@code 0xCC000000}).
     */
    public static Codec<Integer> argbOrInt() {
        return Codec.either(
                Codec.STRING,
                Codec.INT
        ).xmap(
                either -> either.map(ColorUtil::parseArgb, Function.identity()),
                Either::right
        );
    }
}
