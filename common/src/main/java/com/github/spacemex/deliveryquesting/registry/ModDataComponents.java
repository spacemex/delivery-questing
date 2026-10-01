package com.github.spacemex.deliveryquesting.registry;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.progression.MailboxParcel;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;

public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(DeliveryQuesting.MOD_ID, Registries.DATA_COMPONENT_TYPE);

    public static final RegistrySupplier<DataComponentType<MailboxParcel>> MAILBOX_PARCEL;
    public static final RegistrySupplier<DataComponentType<Identifier>> CONTRACT_TASK_ID;

    public static void initialize() {
        DATA_COMPONENTS.register();
    }

    static {
        MAILBOX_PARCEL = DATA_COMPONENTS.register("mailbox_parcel", () -> DataComponentType.<MailboxParcel>builder().persistent(MailboxParcel.CODEC).build());
        CONTRACT_TASK_ID = DATA_COMPONENTS.register("contract_task_id", () -> DataComponentType.<Identifier>builder().persistent(Identifier.CODEC)
                .networkSynchronized(Identifier.STREAM_CODEC).build());
    }
}
