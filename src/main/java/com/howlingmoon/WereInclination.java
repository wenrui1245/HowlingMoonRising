// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.util.StringRepresentable;

public enum WereInclination implements StringRepresentable {
    NEUTRAL("neutral", "Neutral", "Balanced progression with steady XP gain."),
    SKILLFUL("skillful", "Skillful", "Focus on technical abilities. Faster ability unlock speed."),
    MASTERY("mastery", "Mastery", "Focus on attribute power. Increased attribute effectiveness."),
    PREDATOR("predator", "Predator", "Focus on the hunt. Faster level up, but more frequent forced transformations.");

    private final String name;
    private final String displayName;
    private final String description;

    WereInclination(String name, String displayName, String description) {
        this.name = name;
        this.displayName = displayName;
        this.description = description;
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
}
