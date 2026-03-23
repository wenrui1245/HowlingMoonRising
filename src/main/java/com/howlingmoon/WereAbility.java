// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.util.StringRepresentable;

public enum WereAbility implements StringRepresentable {
    HOWL("howl", "Howl", "Produce a powerful howl that affects nearby entities.", 1, 600),
    LEAP("leap", "Leap", "Dash forward and upward with great force.", 1, 100),
    BITE("bite", "Bite", "A vicious bite that deals high damage and poisons the target.", 2, 80),
    CLIMB("climb", "Climb", "Scale vertical surfaces with ease.", 1, 0),
    FEAR("fear", "Fear", "Strike terror into the hearts of your enemies.", 2, 400),
    BERSERK("berserk", "Berserk", "Enter a state of primal rage, increasing damage but also hunger.", 3, 1200),
    MAIM("maim", "Maim", "A crippling strike that slows and weakens the target.", 2, 150),
    SHRED("shred", "Shred", "Rapidly attack the target with claws.", 2, 60),
    RAM("ram", "Ram", "Charge into enemies, knocking them back.", 2, 120),
    LIFT("lift", "Lift", "Pick up and throw smaller entities.", 3, 200),
    NIGHT_VISION("night_vision", "Night Vision", "See clearly in the dark.", 1, 0),
    SCENT_TRACKING("scent_tracking", "Scent Tracking", "Visualize the scent trails of nearby creatures.", 2, 0);

    private final String name;
    private final String displayName;
    private final String description;
    private final int cost;
    private final int baseCooldown; // In ticks

    WereAbility(String name, String displayName, String description, int cost, int baseCooldown) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
        this.cost = cost;
        this.baseCooldown = baseCooldown;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public int getCost() {
        return cost;
    }

    public int getBaseCooldown() {
        return baseCooldown;
    }
}
