package com.github.spacemex.deliveryquesting.registry;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.block.entity.CardboardBoxBlockEntity;
import com.github.spacemex.deliveryquesting.block.entity.ComputerBlockEntity;
import com.github.spacemex.deliveryquesting.block.entity.DronePadBlockEntity;
import com.github.spacemex.deliveryquesting.block.entity.MailboxBlockEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(DeliveryQuesting.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<MailboxBlockEntity>> MAILBOX;
    public static final RegistrySupplier<BlockEntityType<ComputerBlockEntity>> COMPUTER;
    public static final RegistrySupplier<BlockEntityType<CardboardBoxBlockEntity>> CARDBOARD_BOX;
    public static final RegistrySupplier<BlockEntityType<DronePadBlockEntity>> DRONE_PAD;

    public static void initialize() {
        BLOCK_ENTITIES.register();
    }

    private static <T extends BlockEntity> RegistrySupplier<BlockEntityType<T>> register(String name, BlockEntityTypeFactory.Factory<T> factory, Block... blocks) {
        return BLOCK_ENTITIES.register(name, () -> BlockEntityTypeFactory.create(factory, blocks));
    }

    static {
        MAILBOX = register("mailbox", MailboxBlockEntity::new, ModBlocks.MAILBOX.get());
        COMPUTER = register("computer", ComputerBlockEntity::new, ModBlocks.COMPUTER.get());
        CARDBOARD_BOX = register("cardboard_box", CardboardBoxBlockEntity::new,
                ModBlocks.CARDBOARD_BOX_TIER_1.get(),
                ModBlocks.CARDBOARD_BOX_TIER_2.get(),
                ModBlocks.CARDBOARD_BOX_TIER_3.get(),
                ModBlocks.CARDBOARD_BOX_TIER_4.get(),
                ModBlocks.CARDBOARD_BOX_TIER_5.get(),
                ModBlocks.CARDBOARD_BOX_TIER_6.get()
        );
        DRONE_PAD = register("drone_pad", DronePadBlockEntity::new, ModBlocks.DRONE_PAD.get());
    }
}