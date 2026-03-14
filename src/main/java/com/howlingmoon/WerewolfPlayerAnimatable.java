// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.world.entity.player.Player;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.*;
import software.bernie.geckolib.util.GeckoLibUtil;

public class WerewolfPlayerAnimatable implements GeoAnimatable {

    private static final WerewolfPlayerAnimatable INSTANCE = new WerewolfPlayerAnimatable();
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static WerewolfPlayerAnimatable getInstance() {
        return INSTANCE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            Player player = state.getData(software.bernie.geckolib.constant.DataTickets.ENTITY) instanceof Player p ? p : null;
            if (player != null && player.getDeltaMovement().horizontalDistanceSqr() > 0.001) {
                return state.setAndContinue(RawAnimation.begin().thenLoop("animation.werewolf.walk"));
            }
            return state.setAndContinue(RawAnimation.begin().thenLoop("animation.werewolf.idle"));
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

    @Override
    public double getTick(Object o) {
        if (o instanceof Player player) {
            return player.tickCount;
        }
        return 0;
    }
}