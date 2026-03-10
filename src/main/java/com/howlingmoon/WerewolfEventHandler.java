package com.howlingmoon;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = HowlingMoon.MODID)
public class WerewolfEventHandler {

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();

        // Solo en el servidor
        if (player.level().isClientSide()) return;

        // Obtener los datos del jugador
        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);

        // Si está transformado, aplicar efectos
        if (cap.isTransformed()) {
            // Velocidad aumentada
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 40, 1, false, false));
            // Fuerza aumentada
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 40, 1, false, false));
            // Salto aumentado
            player.addEffect(new MobEffectInstance(MobEffects.JUMP, 40, 2, false, false));
            // Visión nocturna
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400, 0, false, false));
        }
    }
}