// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = HowlingMoon.MODID)
public class WerewolfEventHandler {

    // =====================
    //   LOGIN / RESPAWN
    // =====================

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        syncToClient(player);
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (cap.isTransformed()) {
            WerewolfAttributeHandler.applyAllModifiers(player, cap);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        syncToClient(player);
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (cap.isTransformed()) {
            WerewolfAttributeHandler.applyAllModifiers(player, cap);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            WerewolfCapability oldCap = event.getOriginal().getData(WerewolfAttachment.WEREWOLF_DATA);
            WerewolfCapability newCap = event.getEntity().getData(WerewolfAttachment.WEREWOLF_DATA);

            newCap.setWerewolf(oldCap.isWerewolf());
            newCap.setTransformed(false);
            newCap.setMoonForced(false);
            newCap.setLevel(oldCap.getLevel());
            newCap.setExperience(oldCap.getExperience());
            newCap.setUsedAttributePoints(oldCap.getUsedAttributePoints());
            newCap.setUsedAbilityPoints(oldCap.getUsedAbilityPoints());
            newCap.setUnlockedAbilities(new java.util.HashSet<>(oldCap.getUnlockedAbilities()));
            newCap.setSelectedAbility(oldCap.getSelectedAbility());
            newCap.setAttributeTree(new java.util.HashMap<>(oldCap.getAttributeTree()));
        }
    }

    // =====================
    //   LUNA LLENA
    // =====================

    @SubscribeEvent
    public static void onPlayerTickMoon(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        if (player.tickCount % 20 != 0) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isWerewolf()) return;

        WerewolfAbilityHandler.tick(player);

        long dayTime = player.level().getDayTime() % 24000;
        boolean isNight = dayTime >= 13000 && dayTime <= 23000;
        boolean isFullMoon = player.level().getMoonPhase() == 0;
        boolean shouldBeForced = isNight && isFullMoon;

        if (shouldBeForced && !cap.isTransformed()) {
            cap.setTransformed(true);
            cap.setMoonForced(true);
            WerewolfAttributeHandler.applyAllModifiers(player, cap);
            syncToClient(player);
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                            "§c☾ The full moon rises... the beast takes control!"
                    )
            );
        } else if (shouldBeForced && cap.isTransformed() && !cap.isMoonForced()) {
            cap.setMoonForced(true);
            syncToClient(player);
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                            "§c☾ The full moon rises... you cannot return to human form!"
                    )
            );
        } else if (!shouldBeForced && cap.isMoonForced()) {
            cap.setTransformed(false);
            cap.setMoonForced(false);
            WerewolfAttributeHandler.removeAllModifiers(player);
            syncToClient(player);
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                            "§7The moon sets... you return to yourself."
                    )
            );
        }
    }

    // =====================
    //   XP AL MATAR
    // =====================

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isWerewolf() || !cap.isTransformed()) return;

        LivingEntity killed = event.getEntity();
        int xpGained = 0;

        if (killed instanceof Player) {
            xpGained = 50;
        } else if (killed instanceof Monster) {
            xpGained = 15;
        } else if (killed instanceof Animal) {
            xpGained = 5;
        }

        if (xpGained <= 0) return;

        if (cap.getInclination() == WereInclination.PREDATOR) {
            xpGained = (int) (xpGained * 1.5);
        }

        int levelBefore = cap.getLevel();
        cap.addExperience(xpGained);
        int levelAfter = cap.getLevel();

        if (levelAfter > levelBefore) {
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                            "§6⚠ The beast grows stronger! Level " + levelAfter
                    )
            );
        }

        syncToClient(player);
    }

    @SubscribeEvent
    public static void onPlayerTickClimb(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isWerewolf() || !cap.isTransformed() || !cap.getUnlockedAbilities().contains(WereAbility.CLIMB)) return;

        if (player.horizontalCollision && !player.onGround()) {
            net.minecraft.world.phys.Vec3 delta = player.getDeltaMovement();
            // Subir si mira hacia la pared y se mueve hacia adelante
            if (player.zza > 0) {
                player.setDeltaMovement(delta.x, 0.2, delta.z);
                player.fallDistance = 0;
            } else if (delta.y < 0) {
                // Deslizarse lentamente hacia abajo
                player.setDeltaMovement(delta.x, -0.1, delta.z);
                player.fallDistance = 0;
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerWakeUp(net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (player.level().isClientSide()) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isWerewolf()) return;

        int level = cap.getLevel();
        if (level % 5 == 0 && !cap.hasCompletedTrialFor(level)) {
            // Check for Moon Pearl
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                net.minecraft.world.item.ItemStack stack = player.getInventory().getItem(i);
                if (stack.is(HMItems.MOON_PEARL.get())) {
                    stack.shrink(1);
                    cap.completeTrial(level);
                    syncToClient(player);
                    player.sendSystemMessage(
                            net.minecraft.network.chat.Component.literal(
                                    "§d✨ The Moon Pearl resonates... your path forward is clear!"
                            )
                    );
                    return;
                }
            }
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                            "§5⚠ Your progress is stalled. You feel a pull towards a Moon Pearl..."
                    )
            );
        }
    }

    // =====================
    //   HELPER
    // =====================

    private static void syncToClient(ServerPlayer player) {
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        PacketDistributor.sendToPlayer(player, SyncWerewolfPacket.fromCap(cap));
    }
}