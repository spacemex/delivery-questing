package com.github.spacemex.deliveryquesting.registry;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.block.entity.CardboardBoxBlockEntity;
import com.github.spacemex.deliveryquesting.block.entity.ComputerBlockEntity;
import com.github.spacemex.deliveryquesting.block.entity.MailboxBlockEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(DeliveryQuesting.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<MailboxBlockEntity>> MAILBOX;
    public static final RegistrySupplier<BlockEntityType<ComputerBlockEntity>> COMPUTER;
    public static final RegistrySupplier<BlockEntityType<CardboardBoxBlockEntity>> CARDBOARD_BOX_TIER_1;

    public static void initialize() {
        BLOCK_ENTITIES.register();
    }

    static {
        MAILBOX = BLOCK_ENTITIES.register("mailbox", () -> BlockEntityTypeFactory.create(MailboxBlockEntity::new, ModBlocks.MAILBOX.get()));
        COMPUTER = BLOCK_ENTITIES.register("computer", () -> BlockEntityTypeFactory.create(ComputerBlockEntity::new, ModBlocks.COMPUTER.get()));
        CARDBOARD_BOX_TIER_1 = BLOCK_ENTITIES.register("cardboard_box_tier_1", () -> BlockEntityTypeFactory.create(CardboardBoxBlockEntity::new, ModBlocks.CARDBOARD_BOX_TIER_1.get()));
    }
}