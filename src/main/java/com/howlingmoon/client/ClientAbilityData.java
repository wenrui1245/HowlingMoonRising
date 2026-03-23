// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon.client;

import com.howlingmoon.WereAbility;
import java.util.HashMap;
import java.util.Map;

public class ClientAbilityData {

    private static final Map<WereAbility, Integer> cooldowns = new HashMap<>();

    public static void setCooldown(WereAbility ability, int ticks) {
        if (ticks <= 0) {
            cooldowns.remove(ability);
        } else {
            cooldowns.put(ability, ticks);
        }
    }

    public static int getCooldown(WereAbility ability) {
        return cooldowns.getOrDefault(ability, 0);
    }

    public static float getCooldownPercent(WereAbility ability) {
        int current = getCooldown(ability);
        if (current <= 0) return 0;
        return (float) current / ability.getBaseCooldown();
    }

    public static void tick() {
        cooldowns.entrySet().removeIf(entry -> {
            int timeLeft = entry.getValue() - 1;
            if (timeLeft <= 0) return true;
            entry.setValue(timeLeft);
            return false;
        });
    }
}
