// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = HowlingMoon.MODID)
public class WerewolfAttributeHandler {

    private static final ResourceLocation RL_PROTECTION  = rl("were_protection");
    private static final ResourceLocation RL_SPEED       = rl("were_speed");
    private static final ResourceLocation RL_KNOCKBACK   = rl("were_knockback");
    private static final ResourceLocation RL_KNOCKRESIST = rl("were_knockresist");
    private static final ResourceLocation RL_JUMP        = rl("were_jump");

    private static final Set<Holder<MobEffect>> NEGATIVE_EFFECTS = Set.of(
            MobEffects.POISON,
            MobEffects.WITHER,
            MobEffects.BLINDNESS,
            MobEffects.WEAKNESS,
            MobEffects.MOVEMENT_SLOWDOWN,
            MobEffects.HUNGER
    );

    private static ResourceLocation rl(String path) {
        return ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, path);
    }

    private static final Map<UUID, Boolean> lastTransformState = new HashMap<>();

    // =====================
    //   TICK DEL JUGADOR
    // =====================

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        boolean isTransformed = cap.isTransformed();
        boolean wasTransformed = lastTransformState.getOrDefault(player.getUUID(), false);

        if (isTransformed != wasTransformed) {
            lastTransformState.put(player.getUUID(), isTransformed);
            if (isTransformed) {
                applyAllModifiers(player, cap);
            } else {
                removeAllModifiers(player);
            }
        }

        if (!isTransformed) return;

        // REGENERATION: cura vida directamente cada 2 segundos
        if (player.tickCount % 40 == 0) {
            int regenLevel = cap.getAttributeLevel(WereAttribute.REGENERATION);
            if (regenLevel > 0 && player.getHealth() < player.getMaxHealth()) {
                player.heal(regenLevel * 0.5f);
            }
        }

        // HUNGER: reduce exhaustion cada segundo
        if (player.tickCount % 20 == 0) {
            int hungerLevel = cap.getAttributeLevel(WereAttribute.HUNGER);
            if (hungerLevel > 0) {
                float reduction = hungerLevel * 0.04f;
                player.getFoodData().addExhaustion(-reduction);
            }
        }
    }

    // =====================
    //   STRENGTH — solo mano vacía
    // =====================

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isTransformed()) return;

        int strengthLevel = cap.getAttributeLevel(WereAttribute.STRENGTH);
        if (strengthLevel <= 0) return;

        // Solo con la mano vacía
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty()) return;

        if (event.getTarget() instanceof net.minecraft.world.entity.LivingEntity target) {
            float bonusDamage = strengthLevel * 3.0f;
            target.hurt(
                    player.damageSources().playerAttack(player),
                    bonusDamage
            );
        }
    }

    // =====================
    //   RESISTANCE — daño entrante
    // =====================

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isTransformed()) return;

        int resistanceLevel = cap.getAttributeLevel(WereAttribute.RESISTANCE);
        if (resistanceLevel <= 0) return;

        // 1/3 = 20%, 2/3 = 40%, 3/3 = 60%
        float[] reductionPerLevel = {0.20f, 0.40f, 0.60f};
        float reduction = reductionPerLevel[resistanceLevel - 1];
        event.setAmount(event.getAmount() * (1.0f - reduction));
    }

    // =====================
    //   CLARITY
    // =====================

    @SubscribeEvent
    public static void onEffectApplied(MobEffectEvent.Applicable event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isTransformed()) return;

        int clarityLevel = cap.getAttributeLevel(WereAttribute.CLARITY);
        if (clarityLevel <= 0) return;

        MobEffectInstance instance = event.getEffectInstance();
        if (instance == null) return;

        if (NEGATIVE_EFFECTS.contains(instance.getEffect())) {
            float reduction = clarityLevel * 0.2f;
            int newDuration = (int)(instance.getDuration() * (1.0f - reduction));
            if (newDuration <= 0) {
                event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            } else {
                event.getEntity().addEffect(new MobEffectInstance(
                        instance.getEffect(),
                        newDuration,
                        instance.getAmplifier(),
                        instance.isAmbient(),
                        instance.isVisible()
                ));
                event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            }
        }
    }

    // =====================
    //   APLICAR ATRIBUTOS
    // =====================

    public static void applyAllModifiers(ServerPlayer player, WerewolfCapability cap) {
        int protection = cap.getAttributeLevel(WereAttribute.PROTECTION);
        if (protection > 0)
            addModifier(player, Attributes.ARMOR, RL_PROTECTION,
                    protection * 1.0, AttributeModifier.Operation.ADD_VALUE);

        int speed = cap.getAttributeLevel(WereAttribute.SPEED);
        if (speed > 0)
            addModifier(player, Attributes.MOVEMENT_SPEED, RL_SPEED,
                    speed * 0.02, AttributeModifier.Operation.ADD_VALUE);

        int knockback = cap.getAttributeLevel(WereAttribute.KNOCKBACK);
        if (knockback > 0)
            addModifier(player, Attributes.ATTACK_KNOCKBACK, RL_KNOCKBACK,
                    knockback * 0.5, AttributeModifier.Operation.ADD_VALUE);

        int knockresist = cap.getAttributeLevel(WereAttribute.KNOCKRESIST);
        if (knockresist > 0)
            addModifier(player, Attributes.KNOCKBACK_RESISTANCE, RL_KNOCKRESIST,
                    knockresist * 0.05, AttributeModifier.Operation.ADD_VALUE);

        int jump = cap.getAttributeLevel(WereAttribute.JUMP);
        if (jump > 0)
            addModifier(player, Attributes.JUMP_STRENGTH, RL_JUMP,
                    jump * 0.1, AttributeModifier.Operation.ADD_VALUE);
    }

    public static void removeAllModifiers(ServerPlayer player) {
        removeModifier(player, Attributes.ARMOR,                RL_PROTECTION);
        removeModifier(player, Attributes.MOVEMENT_SPEED,       RL_SPEED);
        removeModifier(player, Attributes.ATTACK_KNOCKBACK,     RL_KNOCKBACK);
        removeModifier(player, Attributes.KNOCKBACK_RESISTANCE, RL_KNOCKRESIST);
        removeModifier(player, Attributes.JUMP_STRENGTH,        RL_JUMP);
    }

    private static void addModifier(ServerPlayer player,
                                    Holder<Attribute> attribute,
                                    ResourceLocation id, double value,
                                    AttributeModifier.Operation operation) {
        var instance = player.getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(id);
        instance.addTransientModifier(new AttributeModifier(id, value, operation));
    }

    private static void removeModifier(ServerPlayer player,
                                       Holder<Attribute> attribute,
                                       ResourceLocation id) {
        var instance = player.getAttribute(attribute);
        if (instance != null) instance.removeModifier(id);
    }
}