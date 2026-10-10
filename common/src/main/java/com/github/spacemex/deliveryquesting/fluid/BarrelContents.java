
package com.github.spacemex.deliveryquesting.fluid;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.Objects;

public record BarrelContents(Identifier fluid, int amount) {

    public static final Identifier EMPTY_FLUID = Identifier.withDefaultNamespace("empty");
    public static final Identifier WATER = Identifier.withDefaultNamespace("water");
    public static final Identifier LAVA = Identifier.withDefaultNamespace("lava");

    public static final BarrelContents EMPTY = new BarrelContents(EMPTY_FLUID, 0);

    public static final Codec<BarrelContents> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Identifier.CODEC.fieldOf("fluid")
                            .forGetter(BarrelContents::fluid),
                    Codec.INT.fieldOf("amount")
                            .forGetter(BarrelContents::amount)).apply(instance, BarrelContents::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, BarrelContents> STREAM_CODEC =
            StreamCodec.composite(Identifier.STREAM_CODEC, BarrelContents::fluid, ByteBufCodecs.VAR_INT,
                    BarrelContents::amount, BarrelContents::new);

    public BarrelContents {
        Objects.requireNonNull(fluid, "fluid");

        if (amount < 0) {
            throw new IllegalArgumentException("Negative fluid amount");
        }

        if (amount == 0) {
            fluid = EMPTY_FLUID;
        } else if (fluid.equals(EMPTY_FLUID)) {
            throw new IllegalArgumentException("Non-empty amount requires a fluid");
        }
    }

    public boolean isEmpty() {
        return amount == 0;
    }
}
