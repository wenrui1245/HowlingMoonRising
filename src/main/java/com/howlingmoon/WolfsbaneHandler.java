// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;

@EventBusSubscriber(modid = HowlingMoon.MODID)
public class WolfsbaneHandler {

    @SubscribeEvent
    public static void onItemFinishedUsing(LivingEntityUseItemEvent.Finish event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        ItemStack stack = event.getItem();
        if (!stack.is(HMItems.WOLFSBANE_POTION.get())) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);

        if (!cap.isWerewolf()) {
            player.sendSystemMessage(
                    net.minecraft.network.chat.Component.literal(
                            "§7The potion has no effect on you."
                    )
            );
            return;
        }

        // Si está transformado, quitar modificadores primero
        if (cap.isTransformed()) {
            WerewolfAttributeHandler.removeAllModifiers(player);
        }

        // Curar completamente — resetear toda la capability
        cap.reset();

        WerewolfCommand.syncToClient(player, cap);

        player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal(
                        "§aThe wolfsbane subdues the beast... you are no longer a werewolf."
                )
        );
    }
}
