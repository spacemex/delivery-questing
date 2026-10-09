package com.github.spacemex.deliveryquesting.client.render;

import com.github.spacemex.deliveryquesting.DeliveryQuesting;
import com.github.spacemex.deliveryquesting.entity.DroneEntity;
import com.github.spacemex.deliveryquesting.item.CardboardBoxItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.NonNull;

public final class DroneRenderer extends EntityRenderer<DroneEntity, DroneRenderState> {

    private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();

    private static final Identifier DRONE_TEXTURE =
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/entity/drone.png");
    private static final Identifier ROTOR_TEXTURE =
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/entity/rotor.png");
    private static final Identifier ROPE_TEXTURE =
            Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "textures/entity/rope.png");
    private static final ObjModel DRONE_MODEL =
            new ObjModel(Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "models/entity/drone.obj"));
    private static final ObjModel ROTOR_MODEL =
            new ObjModel(Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "models/entity/rotor.obj"));
    private static final ObjModel ROPE_MODEL =
            new ObjModel(Identifier.fromNamespaceAndPath(DeliveryQuesting.MOD_ID, "models/entity/rope.obj"));

    private final BlockModelResolver blockModelResolver;

    public DroneRenderer(EntityRendererProvider.Context context) {
        super(context);

        blockModelResolver = context.getBlockModelResolver();
    }

    @Override
    public @NonNull DroneRenderState createRenderState() {
        return new DroneRenderState();
    }

    @Override
    public void extractRenderState(@NonNull DroneEntity entity, @NonNull DroneRenderState state, float partialTick) {
        super.extractRenderState(entity, state, partialTick);

        ItemStack payload = entity.getPayload();

        state.hasPayload = payload.getItem() instanceof CardboardBoxItem;

        if (state.hasPayload
                && payload.getItem()
                instanceof CardboardBoxItem boxItem) {

            blockModelResolver.update(state.payload, boxItem.getBlock().defaultBlockState(), DISPLAY_CONTEXT);
        }

        float propellerSpeed =
                switch (entity.getFlightState()) {
                    case DEPARTING -> 1F;
                    case RETURNING -> 0.75F;
                    case IDLE -> 0F;
                };

        state.propellerRotation = (entity.tickCount + partialTick) * 128F * propellerSpeed;
    }

    @Override
    public void submit(@NonNull DroneRenderState state, @NonNull PoseStack poseStack,
                       @NonNull SubmitNodeCollector collector, @NonNull CameraRenderState cameraState) {
        super.submit(
                state,
                poseStack,
                collector,
                cameraState
        );

        poseStack.pushPose();

        poseStack.translate(
                0D,
                1D / 16D,
                0D
        );

        poseStack.scale(
                1F / 16F,
                1F / 16F,
                1F / 16F
        );

        DRONE_MODEL.submit(
                DRONE_TEXTURE,
                poseStack,
                collector,
                state.lightCoords
        );

        poseStack.popPose();

        submitRotor(
                state,
                poseStack,
                collector,
                4D / 16D,
                5D / 16D
        );

        submitRotor(
                state,
                poseStack,
                collector,
                -4D / 16D,
                5D / 16D
        );

        submitRotor(
                state,
                poseStack,
                collector,
                4D / 16D,
                -5D / 16D
        );

        submitRotor(
                state,
                poseStack,
                collector,
                -4D / 16D,
                -5D / 16D
        );

        if (state.hasPayload) {
            submitRope(
                    state,
                    poseStack,
                    collector
            );

            submitPayload(
                    state,
                    poseStack,
                    collector
            );
        }
    }

    private void submitRotor(DroneRenderState state, PoseStack poseStack, SubmitNodeCollector collector,
                             double offsetX, double offsetZ) {
        poseStack.pushPose();

        poseStack.translate(
                offsetX,
                1D / 16D,
                offsetZ
        );

        poseStack.scale(
                1F / 16F,
                1F / 16F,
                1F / 16F
        );

        poseStack.mulPose(Axis.YP.rotationDegrees(-state.propellerRotation));

        ROTOR_MODEL.submit(
                ROTOR_TEXTURE,
                poseStack,
                collector,
                state.lightCoords
        );

        poseStack.popPose();
    }

    private void submitRope(DroneRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        poseStack.pushPose();

        poseStack.translate(
                0D,
                -8D / 16D,
                0D
        );

        poseStack.scale(
                1F / 16F,
                1F / 16F,
                1F / 16F
        );

        ROPE_MODEL.submit(
                ROPE_TEXTURE,
                poseStack,
                collector,
                state.lightCoords
        );

        poseStack.popPose();
    }

    private void submitPayload(DroneRenderState state, PoseStack poseStack, SubmitNodeCollector collector) {
        poseStack.pushPose();

        poseStack.translate(
                -0.5D,
                -1.5D,
                -0.5D
        );

        poseStack.translate(
                0.5D / 16D,
                0.5D / 16D,
                0.5D / 16D
        );

        poseStack.scale(
                15F / 16F,
                15F / 16F,
                15F / 16F
        );

        state.payload.submit(
                poseStack,
                collector,
                state.lightCoords,
                OverlayTexture.NO_OVERLAY,
                state.outlineColor
        );

        poseStack.popPose();
    }
}