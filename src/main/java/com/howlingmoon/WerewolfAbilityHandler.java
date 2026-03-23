// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import com.howlingmoon.network.AbilityCooldownPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class WerewolfAbilityHandler {

    private static final Map<UUID, Map<WereAbility, Integer>> cooldowns = new HashMap<>();

    public static void tick(ServerPlayer player) {
        Map<WereAbility, Integer> playerCooldowns = cooldowns.get(player.getUUID());
        if (playerCooldowns != null) {
            playerCooldowns.entrySet().removeIf(entry -> {
                int timeLeft = entry.getValue() - 1;
                if (timeLeft <= 0) return true;
                entry.setValue(timeLeft);
                return false;
            });
            if (playerCooldowns.isEmpty()) {
                cooldowns.remove(player.getUUID());
            }
        }
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
                
                // Fear effect to nearby entities
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
                // Find entity in front
                Vec3 pos = player.getEyePosition();
                Vec3 look = player.getLookAngle();
                Vec3 reach = pos.add(look.x * 3, look.y * 3, look.z * 3);
                AABB box = player.getBoundingBox().expandTowards(look.scale(3)).inflate(1);
                
                for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != player)) {
                    if (target.getBoundingBox().clip(pos, reach).isPresent()) {
                        target.hurt(player.damageSources().mobAttack(player), 10.0F);
                        target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                        player.heal(2.0F);
                        return true;
                    }
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
                // We'll handle the "impact" in an event or after a short delay
                return true;
            }
            case MAIM -> {
                // Similar to Bite but different effects
                Vec3 pos = player.getEyePosition();
                Vec3 look = player.getLookAngle();
                Vec3 reach = pos.add(look.x * 3, look.y * 3, look.z * 3);
                AABB box = player.getBoundingBox().expandTowards(look.scale(3)).inflate(1);
                
                for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != player)) {
                    if (target.getBoundingBox().clip(pos, reach).isPresent()) {
                        target.hurt(player.damageSources().mobAttack(player), 12.0F);
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 2));
                        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 1));
                        return true;
                    }
                }
                return false;
            }
            case SCENT_TRACKING -> {
                // Glow nearby entities for the player
                AABB area = player.getBoundingBox().inflate(32);
                player.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != player).forEach(e -> {
                    e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, false));
                });
                return true;
            }
            case LIFT -> {
                // For now, let's just make it a strong knockback/throw
                Vec3 pos = player.getEyePosition();
                Vec3 look = player.getLookAngle();
                Vec3 reach = pos.add(look.x * 3, look.y * 3, look.z * 3);
                AABB box = player.getBoundingBox().expandTowards(look.scale(3)).inflate(1);
                
                for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, box, e -> e != player)) {
                    if (target.getBoundingBox().clip(pos, reach).isPresent()) {
                        target.setDeltaMovement(look.x * 2.0, 1.0, look.z * 2.0);
                        target.hurtMarked = true;
                        return true;
                    }
                }
                return false;
            }
            case SHRED -> {
                // Rapid low damage attacks in a cone
                AABB area = player.getBoundingBox().inflate(3);
                player.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != player).forEach(e -> {
                    e.hurt(player.damageSources().mobAttack(player), 4.0F);
                    e.invulnerableTime = 0; // Allow rapid hits
                });
                return true;
            }
            case FEAR -> {
                // Stun/Slow nearby entities
                AABB area = player.getBoundingBox().inflate(8);
                player.level().getEntitiesOfClass(LivingEntity.class, area, e -> e != player).forEach(e -> {
                    e.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 4));
                    e.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 2));
                });
                return true;
            }
            case BERSERK -> {
                // Buff player
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

    public static boolean isOnCooldown(ServerPlayer player, WereAbility ability) {
        Map<WereAbility, Integer> playerCooldowns = cooldowns.get(player.getUUID());
        return playerCooldowns != null && playerCooldowns.containsKey(ability);
    }

    public static void setCooldown(ServerPlayer player, WereAbility ability, int ticks) {
        cooldowns.computeIfAbsent(player.getUUID(), k -> new HashMap<>()).put(ability, ticks);
        // Sync cooldown to client
        PacketDistributor.sendToPlayer(player, new AbilityCooldownPacket(ability, ticks));
    }
}
