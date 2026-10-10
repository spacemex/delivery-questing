package com.github.spacemex.deliveryquesting.registry;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.block.entity.*;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.Arrays;

public final class ModBlockEntities {

    private ModBlockEntities() {
    }

    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(DeliveryQuesting.MOD_ID, Registries.BLOCK_ENTITY_TYPE);

    public static final RegistrySupplier<BlockEntityType<MailboxBlockEntity>> MAILBOX;
    public static final RegistrySupplier<BlockEntityType<ComputerBlockEntity>> COMPUTER;
    public static final RegistrySupplier<BlockEntityType<CardboardBoxBlockEntity>> CARDBOARD_BOX;
    public static final RegistrySupplier<BlockEntityType<DronePadBlockEntity>> DRONE_PAD;
    public static final RegistrySupplier<BlockEntityType<PackagerBlockEntity>> PACKAGER;
    public static final RegistrySupplier<BlockEntityType<BarrelBlockEntity>> BARREL;

    public static void initialize() {
        BLOCK_ENTITIES.register();
    }

    @SafeVarargs
    private static <T extends BlockEntity>
    RegistrySupplier<BlockEntityType<T>> register(String name, BlockEntityTypeFactory.Factory<T> factory,
                                                  RegistrySupplier<? extends Block>... blocks) {
        return BLOCK_ENTITIES.register(name, () -> {
            Block[] resolvedBlocks = Arrays.stream(blocks).map(RegistrySupplier::get).toArray(Block[]::new);

            return BlockEntityTypeFactory.create(factory, resolvedBlocks);
        });
    }

    static {
        MAILBOX = register("mailbox", MailboxBlockEntity::new, ModBlocks.MAILBOX);
        COMPUTER = register("computer", ComputerBlockEntity::new, ModBlocks.COMPUTER);
        CARDBOARD_BOX = register("cardboard_box", CardboardBoxBlockEntity::new,
                ModBlocks.CARDBOARD_BOX_TIER_1,
                ModBlocks.CARDBOARD_BOX_TIER_2,
                ModBlocks.CARDBOARD_BOX_TIER_3,
                ModBlocks.CARDBOARD_BOX_TIER_4,
                ModBlocks.CARDBOARD_BOX_TIER_5,
                ModBlocks.CARDBOARD_BOX_TIER_6);
        DRONE_PAD = register("drone_pad", DronePadBlockEntity::new, ModBlocks.DRONE_PAD);
        PACKAGER = register("packager", PackagerBlockEntity::new, ModBlocks.PACKAGER);
        BARREL = register("barrel", BarrelBlockEntity::new,
                ModBlocks.BARREL_BLOCK_TIER_1,
                ModBlocks.BARREL_BLOCK_TIER_2,
                ModBlocks.BARREL_BLOCK_TIER_3,
                ModBlocks.BARREL_BLOCK_TIER_4,
                ModBlocks.BARREL_BLOCK_TIER_5,
                ModBlocks.BARREL_BLOCK_TIER_6);
    }
}