// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

    private static final Set<Item> MEAT_FOODS = Set.of(
            Items.BEEF, Items.COOKED_BEEF,
            Items.PORKCHOP, Items.COOKED_PORKCHOP,
            Items.CHICKEN, Items.COOKED_CHICKEN,
            Items.MUTTON, Items.COOKED_MUTTON,
            Items.RABBIT, Items.COOKED_RABBIT,
            Items.COD, Items.COOKED_COD,
            Items.SALMON, Items.COOKED_SALMON,
            Items.TROPICAL_FISH,
            Items.ROTTEN_FLESH
    );

    private static final List<String> CARNIVORE_MESSAGES = List.of(
            "§8[§c⚠§8] §7The beast demands §cflesh§7.",
            "§8[§7✦§8] §cThis has never been alive§7.",
            "§8[§c⚠§8] §7Your instincts §creject §7this.",
            "§8[§7✦§8] §cOnly blood and bone §7will satisfy the wolf.",
            "§8[§c⚠§8] §7The beast within §csneers §7at this."
    );

    private static final Random RANDOM = new Random();

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

        if (player.tickCount % 40 == 0) {
            int regenLevel = cap.getAttributeLevel(WereAttribute.REGENERATION);
            if (regenLevel > 0 && player.getHealth() < player.getMaxHealth()) {
                player.heal(regenLevel * 0.5f);
            }
        }

        if (player.tickCount % 20 == 0) {
            int hungerLevel = cap.getAttributeLevel(WereAttribute.HUNGER);
            if (hungerLevel > 0) {
                player.getFoodData().addExhaustion(-(hungerLevel * 0.04f));
            }
        }
    }

    // =====================
    //   STRENGTH + EXHILARATING
    // =====================

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isTransformed()) return;

        ItemStack mainHand = player.getMainHandItem();

        int strengthLevel = cap.getAttributeLevel(WereAttribute.STRENGTH);
        if (strengthLevel > 0 && mainHand.isEmpty()) {
            if (event.getTarget() instanceof LivingEntity target) {
                target.hurt(player.damageSources().playerAttack(player), strengthLevel * 3.0f);
            }
        }


        int exhilaratingLevel = cap.getAttributeLevel(WereAttribute.EXHILARATING);
        if (exhilaratingLevel > 0 && event.getTarget() instanceof LivingEntity) {
            player.heal(exhilaratingLevel * 0.5f);
        }
    }

    // =====================
    //   RESISTANCE
    // =====================

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isTransformed()) return;

        int resistanceLevel = cap.getAttributeLevel(WereAttribute.RESISTANCE);
        if (resistanceLevel > 0) {
            float[] reductionPerLevel = {0.20f, 0.40f, 0.60f};
            event.setAmount(event.getAmount() * (1.0f - reductionPerLevel[resistanceLevel - 1]));
        }
    }

    // =====================
    //   FALL
    // =====================

    @SubscribeEvent
    public static void onFallDamage(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isTransformed()) return;

        int fallLevel = cap.getAttributeLevel(WereAttribute.FALL);
        if (fallLevel <= 0) return;

        switch (fallLevel) {
            case 1 -> event.setDistance(event.getDistance() * 0.7f);
            case 2 -> event.setDistance(event.getDistance() * 0.4f);
            case 3 -> event.setDistance(0);
        }
    }

    // =====================
    //   MINING
    // =====================

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        var player = event.getEntity();
        if (!player.getMainHandItem().isEmpty()) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isWerewolf() || !cap.isTransformed()) return;

        int miningLevel = cap.getAttributeLevel(WereAttribute.MINING);
        if (miningLevel <= 0) return;

        var block = event.getState();
        if (!block.is(BlockTags.MINEABLE_WITH_PICKAXE) && !block.is(BlockTags.MINEABLE_WITH_AXE)) return;

        float speed = switch (miningLevel) {
            case 1 -> 10.0f;
            case 2 -> 30.0f;
            default -> 60.0f;
        };
        event.setNewSpeed(speed);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player)) return;
        if (!player.getMainHandItem().isEmpty()) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isWerewolf() || !cap.isTransformed()) return;

        int miningLevel = cap.getAttributeLevel(WereAttribute.MINING);
        if (miningLevel <= 0) return;

        var state = event.getState();
        if (!state.is(BlockTags.MINEABLE_WITH_PICKAXE) && !state.is(BlockTags.MINEABLE_WITH_AXE)) return;

        ItemStack fakeTool = switch (miningLevel) {
            case 1 -> new ItemStack(Items.WOODEN_PICKAXE);
            case 2 -> new ItemStack(Items.STONE_PICKAXE);
            default -> new ItemStack(Items.IRON_PICKAXE);
        };

        var serverLevel = (ServerLevel) event.getLevel();
        var pos = event.getPos();

        var drops = net.minecraft.world.level.block.Block.getDrops(
                state, serverLevel, pos,
                serverLevel.getBlockEntity(pos),
                player, fakeTool
        );

        for (ItemStack drop : drops) {
            net.minecraft.world.level.block.Block.popResource(serverLevel, pos, drop);
        }
    }

    // =====================
    //   CARNIVORE DIET
    // =====================

    @SubscribeEvent
    public static void onItemUseStart(LivingEntityUseItemEvent.Start event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isWerewolf()) return;

        ItemStack stack = event.getItem();
        FoodProperties food = stack.get(DataComponents.FOOD);
        if (food == null) return;

        if (!MEAT_FOODS.contains(stack.getItem())) {
            event.setCanceled(true);
            player.displayClientMessage(
                    Component.literal(CARNIVORE_MESSAGES.get(RANDOM.nextInt(CARNIVORE_MESSAGES.size()))),
                    true
            );
        }
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
            int newDuration = (int)(instance.getDuration() * (1.0f - clarityLevel * 0.2f));
            if (newDuration <= 0) {
                event.setResult(MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
            } else {
                event.getEntity().addEffect(new MobEffectInstance(
                        instance.getEffect(), newDuration,
                        instance.getAmplifier(), instance.isAmbient(), instance.isVisible()
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
                    protection * 2.5, AttributeModifier.Operation.ADD_VALUE);

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