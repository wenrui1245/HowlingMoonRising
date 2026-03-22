// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = HowlingMoon.MODID, value = Dist.CLIENT)
public class WerewolfSoundHandler {

    private static boolean wasTransformed = false;
    private static boolean wasMoonForced = false;
    private static boolean wasFullMoonNight = false;

    private static int heartbeatTick = 0;
    private static final int HEARTBEAT_INTERVAL = 30;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();

        if (mc.isPaused()) return;

        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        boolean isTransformed = cap.isTransformed();
        boolean isMoonForced = cap.isMoonForced();

        long dayTime = mc.level.getDayTime() % 24000;
        boolean isNight = dayTime >= 13000 && dayTime <= 23000;
        boolean isFullMoon = mc.level.getMoonPhase() == 0;
        boolean isFullMoonNight = isNight && isFullMoon;

        // Aullido suave — cuando el servidor marca moonForced=true por primera vez
        // Es el momento exacto en que la luna toma el control, sin depender del cliente
        if (isMoonForced && !wasMoonForced && cap.isWerewolf()) {
            playSound(mc, HMSounds.HOWL.get(), 0.8f, 0.9f);
        }
        // Aullido fuerte — transformación manual (moonForced sigue siendo false)
        else if (isTransformed && !wasTransformed && !isMoonForced) {
            playSound(mc, HMSounds.HOWL.get(), 1.0f, 1.0f);
        }

        // Latido cuando tiene poca vida transformado
        if (isTransformed && player.getHealth() < player.getMaxHealth() * 0.3f) {
            heartbeatTick++;
            if (heartbeatTick >= HEARTBEAT_INTERVAL) {
                heartbeatTick = 0;
                if ((player.tickCount / HEARTBEAT_INTERVAL) % 2 == 0) {
                    playSound(mc, HMSounds.HEARTBEAT.get(), 0.7f, 1.0f);
                } else {
                    playSound(mc, HMSounds.HEARTBEAT_DELAY.get(), 0.7f, 1.0f);
                }
            }
        } else {
            heartbeatTick = 0;
        }

        wasTransformed = isTransformed;
        wasMoonForced = isMoonForced;
        wasFullMoonNight = isFullMoonNight;
    }

    private static void playSound(Minecraft mc, net.minecraft.sounds.SoundEvent sound,
                                  float volume, float pitch) {
        mc.getSoundManager().play(
                SimpleSoundInstance.forUI(sound, pitch, volume)
        );
    }
}