package com.github.spacemex.deliveryquesting.client.render;

import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public final class DroneRenderState extends EntityRenderState {
    public final BlockModelRenderState body = new BlockModelRenderState();
    public final BlockModelRenderState payload = new BlockModelRenderState();
    public boolean hasPayload;
}