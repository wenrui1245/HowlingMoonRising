package com.howlingmoon;

public class WerewolfCapability {

    private boolean isWerewolf = false;
    private boolean isTransformed = false;
    private int level = 1;
    private int experience = 0;

    // ¿Es hombre lobo?
    public boolean isWerewolf() { return isWerewolf; }
    public void setWerewolf(boolean werewolf) { isWerewolf = werewolf; }

    // ¿Está transformado ahora mismo?
    public boolean isTransformed() { return isTransformed; }
    public void setTransformed(boolean transformed) { isTransformed = transformed; }

    // Nivel de licantropía
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }

    // Experiencia
    public int getExperience() { return experience; }
    public void setExperience(int experience) { this.experience = experience; }

    // Resetear todo
    public void reset() {
        isWerewolf = false;
        isTransformed = false;
        level = 1;
        experience = 0;
    }
}