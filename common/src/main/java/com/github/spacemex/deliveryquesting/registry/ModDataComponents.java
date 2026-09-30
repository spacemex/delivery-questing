package com.github.spacemex.deliveryquesting.registry;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.progression.MailboxParcel;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;

public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS = DeferredRegister.create(DeliveryQuesting.MOD_ID, Registries.DATA_COMPONENT_TYPE);

    public static final RegistrySupplier<DataComponentType<MailboxParcel>> MAILBOX_PARCEL;

    public static void initialize() {
        DATA_COMPONENTS.register();
    }

    static {
        MAILBOX_PARCEL = DATA_COMPONENTS.register("mailbox_parcel", () -> DataComponentType.<MailboxParcel>builder().persistent(MailboxParcel.CODEC).build());
    }
}
