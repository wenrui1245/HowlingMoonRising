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
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = HowlingMoon.MODID)
public class WerewolfEventHandler {

    // Sincronizar al entrar al mundo
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        syncToClient(player);

        // Si estaba transformado, reaplicar atributos
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (cap.isTransformed()) {
            WerewolfAttributeHandler.applyAllModifiers(player, cap);
        }
    }

    // Sincronizar al respawnear o volver de otra dimensión
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        syncToClient(player);

        // Si estaba transformado, reaplicar atributos
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (cap.isTransformed()) {
            WerewolfAttributeHandler.applyAllModifiers(player, cap);
        }
    }

    // Conservar datos al morir
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (event.isWasDeath()) {
            WerewolfCapability oldCap = event.getOriginal().getData(WerewolfAttachment.WEREWOLF_DATA);
            WerewolfCapability newCap = event.getEntity().getData(WerewolfAttachment.WEREWOLF_DATA);

            newCap.setWerewolf(oldCap.isWerewolf());
            newCap.setTransformed(false); // Al morir vuelve a forma humana
            newCap.setLevel(oldCap.getLevel());
            newCap.setExperience(oldCap.getExperience());
            newCap.setUsedAttributePoints(oldCap.getUsedAttributePoints());
            newCap.setAttributeTree(new java.util.HashMap<>(oldCap.getAttributeTree()));
        }
    }

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

    private static void syncToClient(ServerPlayer player) {
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        PacketDistributor.sendToPlayer(player, new SyncWerewolfPacket(
                cap.isWerewolf(),
                cap.isTransformed(),
                cap.getLevel(),
                cap.getExperience(),
                cap.getUsedAttributePoints(),
                cap.getAttributeTree()
        ));
    }
}