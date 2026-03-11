// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = HowlingMoon.MODID, value = Dist.CLIENT)
public class WerewolfClientHandler {

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        // Tecla H — abrir menú
        if (WerewolfKeyBindings.OPEN_MENU.consumeClick()) {
            WerewolfCapability cap = mc.player.getData(WerewolfAttachment.WEREWOLF_DATA);
            if (cap.isWerewolf()) {
                mc.setScreen(new WerewolfScreen());
            }
        }

        // Tecla J — transformarse
        if (WerewolfKeyBindings.TRANSFORM.consumeClick()) {
            WerewolfCapability cap = mc.player.getData(WerewolfAttachment.WEREWOLF_DATA);
            if (cap.isWerewolf()) {
                PacketDistributor.sendToServer(new TransformPacket());
            }
        }
    }
}