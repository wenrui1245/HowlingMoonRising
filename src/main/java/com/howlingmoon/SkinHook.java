package com.howlingmoon;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;

public class SkinHook {

    public static void setSkin(AbstractClientPlayer player, ResourceLocation skin) {
        // Hook para cambiar la skin temporalmente
        // Se implementa via Mixin en fase posterior
    }
}