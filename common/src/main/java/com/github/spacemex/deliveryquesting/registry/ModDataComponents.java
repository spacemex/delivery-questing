package com.github.spacemex.deliveryquesting.registry;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.fluid.BarrelContents;
import com.github.spacemex.deliveryquesting.progression.entry.MailboxParcel;
import com.mojang.serialization.Codec;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

import java.util.function.UnaryOperator;

public final class ModDataComponents {

    private ModDataComponents() {
    }

    private static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(DeliveryQuesting.MOD_ID, Registries.DATA_COMPONENT_TYPE);

    public static final RegistrySupplier<DataComponentType<MailboxParcel>> MAILBOX_PARCEL;
    public static final RegistrySupplier<DataComponentType<Identifier>> CONTRACT_TASK_ID;
    public static final RegistrySupplier<DataComponentType<Boolean>> MAILBOX_NEW_MAIL;
    public static final RegistrySupplier<DataComponentType<Boolean>> COMPUTER_ON;
    public static final RegistrySupplier<DataComponentType<BarrelContents>> BARREL_CONTENTS;

    public static void initialize() {
        DATA_COMPONENTS.register();
    }

    private static <T> RegistrySupplier<DataComponentType<T>> register(String name, UnaryOperator<DataComponentType.Builder<T>> builder) {
        return DATA_COMPONENTS.register(name, () -> builder.apply(DataComponentType.builder()).build());
    }

    static {
        MAILBOX_PARCEL = register("mailbox_parcel", b -> b.persistent(MailboxParcel.CODEC));
        CONTRACT_TASK_ID = register("contract_task_id", b -> b.persistent(Identifier.CODEC).networkSynchronized(Identifier.STREAM_CODEC));
        MAILBOX_NEW_MAIL = register("mailbox_new_mail", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
        COMPUTER_ON = register("computer_on", b -> b.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));
        BARREL_CONTENTS = register("barrel_contents", b -> b.persistent(BarrelContents.CODEC).networkSynchronized(BarrelContents.STREAM_CODEC));
    }
}
