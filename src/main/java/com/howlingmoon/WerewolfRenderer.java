package com.howlingmoon;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class WerewolfRenderer extends GeoEntityRenderer<WerewolfEntity> {

    public WerewolfRenderer(EntityRendererProvider.Context context) {
        super(context, new WerewolfModel());
    }
}