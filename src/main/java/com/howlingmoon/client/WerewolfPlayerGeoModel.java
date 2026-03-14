// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon.client;

import com.howlingmoon.HowlingMoon;
import com.howlingmoon.WerewolfPlayerAnimatable;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WerewolfPlayerGeoModel extends GeoModel<WerewolfPlayerAnimatable> {

    @Override
    public ResourceLocation getModelResource(WerewolfPlayerAnimatable animatable) {
        return ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "geo/werewolf.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(WerewolfPlayerAnimatable animatable) {
        return ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "textures/entity/werewolf.png");
    }

    @Override
    public ResourceLocation getAnimationResource(WerewolfPlayerAnimatable animatable) {
        return ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "animations/werewolf.animation.json");
    }
}