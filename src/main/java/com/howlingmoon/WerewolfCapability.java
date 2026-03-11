package com.howlingmoon;

import java.util.HashMap;
import java.util.Map;

public class WerewolfCapability {

    // --- Estado básico ---
    private boolean isWerewolf = false;
    private boolean isTransformed = false;

    // --- Sistema de XP y niveles ---
    private int level = 1;
    private int experience = 0;
    private int usedAttributePoints = 0;

    // --- Attribute Tree: guarda el nivel de cada atributo ---
    private Map<String, Integer> attributeTree = new HashMap<>();

    // ========================
    //   GETTERS / SETTERS
    // ========================

    public boolean isWerewolf() { return isWerewolf; }
    public void setWerewolf(boolean werewolf) { isWerewolf = werewolf; }

    public boolean isTransformed() { return isTransformed; }
    public void setTransformed(boolean transformed) { isTransformed = transformed; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    public int getUsedAttributePoints() { return usedAttributePoints; }
    public void setUsedAttributePoints(int points) { this.usedAttributePoints = points; }

    // ========================
    //   SISTEMA XP / NIVELES
    // ========================

    private static final int LEVEL_CAP = 20;

    /** XP necesaria para subir al siguiente nivel */
    public int expNeededForNextLevel() {
        return 50 + (level * 30);
    }

    /** Añade XP y sube de nivel si corresponde. Solo llamar si isTransformed=true */
    public void addExperience(int amount) {
        if (!isTransformed) return;
        experience += amount;
        while (experience >= expNeededForNextLevel() && level < LEVEL_CAP) {
            experience -= expNeededForNextLevel();
            level++;
        }
        if (level >= LEVEL_CAP) {
            experience = 0;
        }
    }

    /** Puntos de atributo disponibles = nivel - puntos ya gastados */
    public int getAvailableAttributePoints() {
        return level - usedAttributePoints;
    }

    // ========================
    //   ATTRIBUTE TREE
    // ========================

    public int getAttributeLevel(WereAttribute attribute) {
        return attributeTree.getOrDefault(attribute.getKey(), 0);
    }

    public boolean canUpgradeAttribute(WereAttribute attribute) {
        return getAvailableAttributePoints() > 0
                && getAttributeLevel(attribute) < attribute.getMaxLevel();
    }

    public void upgradeAttribute(WereAttribute attribute) {
        if (!canUpgradeAttribute(attribute)) return;
        int current = getAttributeLevel(attribute);
        attributeTree.put(attribute.getKey(), current + 1);
        usedAttributePoints++;
    }

    public Map<String, Integer> getAttributeTree() { return attributeTree; }
    public void setAttributeTree(Map<String, Integer> tree) { this.attributeTree = tree; }

    public void resetAttributePoints() {
        attributeTree.clear();
        usedAttributePoints = 0;
    }

    // ========================
    //   RESET
    // ========================

    public void reset() {
        isWerewolf = false;
        isTransformed = false;
        level = 1;
        experience = 0;
        usedAttributePoints = 0;
        attributeTree.clear();
    }
}