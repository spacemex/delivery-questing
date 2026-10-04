package com.github.spacemex.deliveryquesting.client.render;

import com.github.spacemex.deliveryquesting.entity.DroneEntity;
import com.github.spacemex.deliveryquesting.item.CardboardBoxItem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import org.jspecify.annotations.NonNull;

public final class DroneRenderer extends EntityRenderer<DroneEntity, DroneRenderState> {
    private static final BlockDisplayContext DISPLAY_CONTEXT = BlockDisplayContext.create();
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

        blockModelResolver.update(state.body, Blocks.IRON_BLOCK.defaultBlockState(), DISPLAY_CONTEXT);

        ItemStack payload = entity.getPayload();
        state.hasPayload = payload.getItem() instanceof CardboardBoxItem;

        if (state.hasPayload && payload.getItem() instanceof CardboardBoxItem boxItem) {
            blockModelResolver.update(state.payload, boxItem.getBlock().defaultBlockState(), DISPLAY_CONTEXT);
        }
    }

    @Override
    public void submit(@NonNull DroneRenderState state, @NonNull PoseStack poseStack, @NonNull SubmitNodeCollector collector, @NonNull CameraRenderState cameraState) {
        super.submit(state, poseStack, collector, cameraState);
        poseStack.pushPose();
        poseStack.scale(0.65F, 0.18F, 0.65F);
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        state.body.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);

        poseStack.popPose();

        if (state.hasPayload) {
            poseStack.pushPose();
            poseStack.translate(0.0F, -0.55F, 0.0F);
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.translate(-0.5F, -0.5F, -0.5F);
            state.payload.submit(poseStack, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
            poseStack.popPose();
        }
    }
}