// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon.client;

import com.howlingmoon.WerewolfEntity;
import com.howlingmoon.WerewolfModel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class WerewolfGeoRenderer extends GeoEntityRenderer<WerewolfEntity> {

    public WerewolfGeoRenderer(EntityRendererProvider.Context context) {
        super(context, new WerewolfModel());
        this.withScale(0.0625f);
    }

    @Override
    protected void applyRotations(WerewolfEntity animatable, PoseStack poseStack,
                                  float ageInTicks, float rotationYaw,
                                  float partialTick, float nativeScale) {
        super.applyRotations(animatable, poseStack, ageInTicks, rotationYaw, partialTick, nativeScale);
    }
}