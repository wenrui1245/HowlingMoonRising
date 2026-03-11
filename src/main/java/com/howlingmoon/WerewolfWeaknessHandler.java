package com.howlingmoon;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = HowlingMoon.MODID)
public class WerewolfWeaknessHandler {

    private static final TagKey<Item> SILVER_ITEMS = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "silver_items")
    );

    private static final TagKey<Item> SILVER_INGOTS = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("c", "ingots/silver")
    );

    // =====================
    //   DAÑO DE PLATA
    // =====================

    @SubscribeEvent
    public static void onSilverDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isWerewolf() || !cap.isTransformed()) return;

        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) return;

        ItemStack weapon = attacker.getMainHandItem();
        if (weapon.isEmpty()) return;

        boolean isSilver = weapon.is(SILVER_ITEMS) || weapon.is(SILVER_INGOTS);
        if (!isSilver) return;

        event.setAmount(event.getAmount() + 8.0f);
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 1, false, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 0, false, true));
    }

    // =====================
    //   NO PUEDES DORMIR
    // =====================

    @SubscribeEvent
    public static void onSleep(CanPlayerSleepEvent event) {
        Player player = event.getEntity();
        if (player.level().isClientSide()) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isWerewolf() || !cap.isTransformed()) return;

        event.setProblem(Player.BedSleepingProblem.OTHER_PROBLEM);
        player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal(
                        "§cThe beast within will not rest..."
                )
        );
    }

    // =====================
    //   MOBS PASIVOS HUYEN
    // =====================

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;

        if (event.getEntity() instanceof PathfinderMob mob
                && !(event.getEntity() instanceof Monster)) {

            mob.goalSelector.addGoal(1,
                    new AvoidEntityGoal<>(
                            mob,
                            ServerPlayer.class,
                            10.0f, 1.2, 1.5,
                            (LivingEntity entity) -> {
                                if (!(entity instanceof ServerPlayer sp)) return false;
                                WerewolfCapability cap = sp.getData(WerewolfAttachment.WEREWOLF_DATA);
                                return cap.isWerewolf() && cap.isTransformed();
                            }
                    )
            );
        }
    }

    // =====================
    //   SIN ARMADURA
    // =====================

    @SubscribeEvent
    public static void onPlayerTickArmor(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;
        if (player.tickCount % 20 != 0) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isWerewolf() || !cap.isTransformed()) return;

        EquipmentSlot[] armorSlots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        };

        boolean msgSent = false;
        for (EquipmentSlot slot : armorSlots) {
            ItemStack armor = player.getItemBySlot(slot);
            if (armor.isEmpty()) continue;

            if (!player.getInventory().add(armor.copy())) {
                player.drop(armor.copy(), false);
            }
            player.setItemSlot(slot, ItemStack.EMPTY);

            if (!msgSent) {
                player.sendSystemMessage(
                        net.minecraft.network.chat.Component.literal(
                                "§cThe beast rejects your armor!"
                        )
                );
                msgSent = true;
            }
        }
    }
}