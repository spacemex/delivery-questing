package com.github.spacemex.deliveryquesting.registry;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.entity.DroneEntity;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.function.UnaryOperator;

public final class ModEntities {

    private ModEntities() {}

    private static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(DeliveryQuesting.MOD_ID, Registries.ENTITY_TYPE);

    public static final RegistrySupplier<EntityType<DroneEntity>> DRONE;

    private static <T extends Entity> RegistrySupplier<EntityType<T>> register(String name, EntityType.EntityFactory<T> factory, MobCategory category, UnaryOperator<EntityType.Builder<T>> builder) {
        return ENTITY_TYPES.register(name, () -> builder.apply(EntityType.Builder.of(factory, category)).build(key(name)));
    }

    public static void initialize() {
        ENTITY_TYPES.register();
    }

    private static ResourceKey<EntityType<?>> key(String path) {
        return ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, path));
    }

    static {
        DRONE = register("drone", DroneEntity::new, MobCategory.MISC, b -> b.sized(0.8f, 0.45f).clientTrackingRange(10).updateInterval(1).noLootTable());
    }
}