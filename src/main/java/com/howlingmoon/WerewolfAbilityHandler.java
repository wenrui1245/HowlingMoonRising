// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import com.howlingmoon.network.AbilityCooldownPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

public class WerewolfAbilityHandler {

    public static void tick(ServerPlayer player) {
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        cap.tickCooldowns();
    }

    public static void handleAbilityUse(ServerPlayer player, WereAbility ability) {
        if (player == null || ability == null) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isTransformed() || !cap.getUnlockedAbilities().contains(ability)) return;

        if (isOnCooldown(player, ability)) return;

        boolean success = executeAbility(player, ability);

        if (success && ability.getBaseCooldown() > 0) {
            setCooldown(player, ability, ability.getBaseCooldown());
        }
    }

    private static boolean executeAbility(ServerPlayer player, WereAbility ability) {
        switch (ability) {
            case HOWL -> {
                player.level().playSound(null, player.getX(), player.getY(), player.getZ(), 
                        HMSounds.HOWL.get(), player.getSoundSource(), 1.0F, 1.0F);
                
                AABB area = player.getBoundingBox().inflate(10);
                player.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != player).forEach(e -> {
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1));
                });
                return true;
            }
            case LEAP -> {
                Vec3 look = player.getLookAngle();
                player.setDeltaMovement(look.x * 1.5, 0.6, look.z * 1.5);
                player.hurtMarked = true;
                return true;
            }
            case BITE -> {
                // MEJORADO: Raycast preciso (alcance de 3 bloques)
                LivingEntity target = getTargetInFront(player, 3.0D);
                if (target != null) {
                    target.hurt(player.damageSources().mobAttack(player), 10.0F);
                    target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                    player.heal(2.0F);
                    return true;
                }
                return false;
            }
            case NIGHT_VISION -> {
                if (player.hasEffect(MobEffects.NIGHT_VISION)) {
                    player.removeEffect(MobEffects.NIGHT_VISION);
                } else {
                    player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, -1, 0, false, false));
                }
                return true;
            }
            case RAM -> {
                Vec3 look = player.getLookAngle();
                player.setDeltaMovement(look.x * 2.0, 0.2, look.z * 2.0);
                player.hurtMarked = true;
                return true;
            }
            case MAIM -> {
                // MEJORADO: Raycast preciso
                LivingEntity target = getTargetInFront(player, 3.0D);
                if (target != null) {
                    target.hurt(player.damageSources().mobAttack(player), 12.0F);
                    target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 2));
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
                    return true;
                }
                return false;
            }
            case SCENT_TRACKING -> {
                AABB area = player.getBoundingBox().inflate(32);
                player.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != player).forEach(e -> {
                    e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, false));
                });
                return true;
            }
            case LIFT -> {
                LivingEntity target = getTargetInFront(player, 3.0D);
                if (target != null) {
                    Vec3 look = player.getLookAngle();
                    target.setDeltaMovement(look.x * 2.0, 1.0, look.z * 2.0);
                    target.hurtMarked = true;
                    return true;
                }
                return false;
            }
            case SHRED -> {
                AABB area = player.getBoundingBox().inflate(3);
                player.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != player).forEach(e -> {
                    e.hurt(player.damageSources().mobAttack(player), 4.0F);
                    e.invulnerableTime = 0; 
                });
                return true;
            }
            case FEAR -> {
                AABB area = player.getBoundingBox().inflate(8);
                player.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != player).forEach(e -> {
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 4));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2));
                });
                return true;
            }
            case BERSERK -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 600, 1));
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
                player.addEffect(new MobEffectInstance(MobEffects.HUNGER, 600, 0));
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    // NUEVO: Método helper para Raycasting preciso
    private static LivingEntity getTargetInFront(ServerPlayer player, double reach) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 reachVec = eyePos.add(look.x * reach, look.y * reach, look.z * reach);
        AABB box = player.getBoundingBox().expandTowards(look.scale(reach)).inflate(1.0D);

        EntityHitResult hitResult = ProjectileUtil.getEntityHitResult(
                player.level(), player, eyePos, reachVec, box, 
                entity -> !entity.isSpectator() && entity.isPickable() && entity instanceof LivingEntity
        );

        if (hitResult != null && hitResult.getType() == HitResult.Type.ENTITY) {
            return (LivingEntity) hitResult.getEntity();
        }
        return null;
    }

    public static boolean isOnCooldown(ServerPlayer player, WereAbility ability) {
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        return cap.getCooldown(ability) > 0;
    }

    public static void setCooldown(ServerPlayer player, WereAbility ability, int ticks) {
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        cap.setCooldown(ability, ticks);
        PacketDistributor.sendToPlayer(player, new AbilityCooldownPacket(ability, ticks));
    }
}