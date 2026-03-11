package com.howlingmoon;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

public class SkinOverride {

    private static final Map<AbstractClientPlayer, ResourceLocation> originalSkins = new HashMap<>();

    public static void override(AbstractClientPlayer player, ResourceLocation newSkin) {
        if (!originalSkins.containsKey(player)) {
            originalSkins.put(player, player.getSkin().texture());
        }
        SkinHook.setSkin(player, newSkin);
    }

    public static void restore(AbstractClientPlayer player) {
        ResourceLocation original = originalSkins.remove(player);
        if (original != null) {
            SkinHook.setSkin(player, original);
        }
    }
}