package com.howlingmoon;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.animal.armadillo.Armadillo;

public class AvoidWerewolfGoal extends AvoidEntityGoal<ServerPlayer> {

    public AvoidWerewolfGoal(PathfinderMob mob, float distance, double slowSpeed, double fastSpeed) {
        super(mob, ServerPlayer.class, distance, slowSpeed, fastSpeed, (entity) -> {
            if (!(entity instanceof ServerPlayer sp)) return false;
            WerewolfCapability cap = sp.getData(WerewolfAttachment.WEREWOLF_DATA);
            return cap.isWerewolf() && cap.isTransformed();
        });
    }

    @Override
    public boolean canUse() {
        // No huir si están durmiendo
        if (this.mob.isSleeping()) {
            return false;
        }
        
        // No huir si es un armadillo y está en su caparazón
        if (this.mob instanceof Armadillo armadillo) {
            if (armadillo.getState() != Armadillo.ArmadilloState.IDLE) {
                return false;
            }
        }
        
        return super.canUse();
    }

    @Override
    public boolean canContinueToUse() {
        if (this.mob.isSleeping()) return false;
        
        if (this.mob instanceof Armadillo armadillo) {
            if (armadillo.getState() != Armadillo.ArmadilloState.IDLE) {
                return false;
            }
        }
        
        return super.canContinueToUse();
    }
}
