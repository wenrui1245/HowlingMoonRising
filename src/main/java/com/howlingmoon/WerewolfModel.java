// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WerewolfModel extends GeoModel<WerewolfEntity> {

    @Override
    public ResourceLocation getModelResource(WerewolfEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "geo/werewolf_npc.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WerewolfEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "textures/entity/werewolf_npc.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WerewolfEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "animations/werewolf_npc.animation.json");
    }
}