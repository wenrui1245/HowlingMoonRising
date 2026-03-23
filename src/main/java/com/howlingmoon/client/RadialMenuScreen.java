// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon.client;

import com.howlingmoon.HowlingMoon;
import com.howlingmoon.WereAbility;
import com.howlingmoon.WerewolfAttachment;
import com.howlingmoon.WerewolfCapability;
import com.howlingmoon.network.UseAbilityPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class RadialMenuScreen extends Screen {

    private final WerewolfCapability cap;
    private final List<WereAbility> abilities;
    private int selectedIndex = -1;

    public RadialMenuScreen() {
        super(Component.literal("Radial Menu"));
        this.cap = Minecraft.getInstance().player.getData(WerewolfAttachment.WEREWOLF_DATA);
        this.abilities = new ArrayList<>(cap.getUnlockedAbilities());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = width / 2;
        int centerY = height / 2;

        if (abilities.isEmpty()) {
            guiGraphics.drawCenteredString(font, "No abilities unlocked", centerX, centerY, 0xFFFFFFFF);
            return;
        }

        double angleStep = 2 * Math.PI / abilities.size();
        double mouseAngle = Math.atan2(mouseY - centerY, mouseX - centerX);
        if (mouseAngle < 0) mouseAngle += 2 * Math.PI;

        double distSq = (mouseX - centerX) * (mouseX - centerX) + (mouseY - centerY) * (mouseY - centerY);
        if (distSq > 400 && distSq < 10000) {
            selectedIndex = (int) Math.round(mouseAngle / angleStep) % abilities.size();
        } else {
            selectedIndex = -1;
        }

        for (int i = 0; i < abilities.size(); i++) {
            WereAbility ability = abilities.get(i);
            double angle = i * angleStep;
            int x = (int) (centerX + Math.cos(angle) * 60);
            int y = (int) (centerY + Math.sin(angle) * 60);

            int color = (i == selectedIndex) ? 0xFFFFFF00 : 0xFFFFFFFF;
            if (ClientAbilityData.getCooldown(ability) > 0) {
                color = 0xFFFF0000;
            }

            guiGraphics.drawCenteredString(font, ability.getDisplayName(), x, y, color);
        }

        if (selectedIndex != -1) {
            WereAbility sel = abilities.get(selectedIndex);
            guiGraphics.drawCenteredString(font, sel.getDisplayName(), centerX, centerY - 20, 0xFFFFFF00);
            guiGraphics.drawCenteredString(font, sel.getDescription(), centerX, centerY + 20, 0xFFBBBBBB);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (selectedIndex != -1) {
            WereAbility sel = abilities.get(selectedIndex);
            if (ClientAbilityData.getCooldown(sel) <= 0) {
                cap.setSelectedAbility(sel);
                PacketDistributor.sendToServer(new UseAbilityPacket(sel));
                this.onClose();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
