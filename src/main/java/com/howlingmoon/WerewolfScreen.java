// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public class WerewolfScreen extends Screen {

    private static final int WIDTH = 240;
    private static final int HEIGHT = 310;

    private static final int COLOR_BG         = 0xF0100810;
    private static final int COLOR_BORDER      = 0xFF8B0000;
    private static final int COLOR_TITLE       = 0xFFFF4500;
    private static final int COLOR_TEXT        = 0xFFE8D5B0;
    private static final int COLOR_TEXT_DIM    = 0xFF8A7A60;
    private static final int COLOR_XP_BG       = 0xFF2A1A00;
    private static final int COLOR_XP_FILL     = 0xFFFF8C00;
    private static final int COLOR_ATTR_HOVER  = 0xFF2A1010;
    private static final int COLOR_DOT_FILLED  = 0xFFFF4500;
    private static final int COLOR_DOT_EMPTY   = 0xFF3A2020;
    private static final int COLOR_POINTS      = 0xFFFFD700;

    public WerewolfScreen() {
        super(Component.literal("Werewolf"));
    }

    @Override
    public void renderBackground(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        // Sin fondo borroso
    }

    @Override
    protected void init() {
        super.init();
        int left = (this.width - WIDTH) / 2;
        int top  = (this.height - HEIGHT) / 2;

        WerewolfCapability cap = getCapability();
        if (cap == null) return;

        WereAttribute[] attrs = WereAttribute.values();
        int startY = top + 72;
        int rowHeight = 16;

        for (int i = 0; i < attrs.length; i++) {
            WereAttribute attr = attrs[i];
            int btnX = left + WIDTH - 22;
            int btnY = startY + i * rowHeight - 3;

            boolean canUpgrade = cap.isWerewolf()
                    && cap.getAvailableAttributePoints() > 0
                    && cap.canUpgradeAttribute(attr);

            Button btn = Button.builder(Component.literal("+"), b -> {
                        PacketDistributor.sendToServer(new UpgradeAttributePacket(attr.name()));
                        this.clearWidgets();
                        this.init();
                    })
                    .bounds(btnX, btnY, 14, 10)
                    .build();

            btn.active = canUpgrade;
            this.addRenderableWidget(btn);
        }
    }

    @Override
    public void render(GuiGraphics gfx, int mouseX, int mouseY, float partialTick) {
        int left = (this.width - WIDTH) / 2;
        int top  = (this.height - HEIGHT) / 2;

        // Fondo
        gfx.fill(left, top, left + WIDTH, top + HEIGHT, COLOR_BG);

        // Bordes
        gfx.fill(left,           top,            left + WIDTH,     top + 1,          COLOR_BORDER);
        gfx.fill(left,           top + HEIGHT-1, left + WIDTH,     top + HEIGHT,     COLOR_BORDER);
        gfx.fill(left,           top,            left + 1,         top + HEIGHT,     COLOR_BORDER);
        gfx.fill(left + WIDTH-1, top,            left + WIDTH,     top + HEIGHT,     COLOR_BORDER);

        WerewolfCapability cap = getCapability();
        if (cap == null) {
            gfx.drawCenteredString(this.font, "§cYou are not a werewolf.", left + WIDTH/2, top + HEIGHT/2, 0xFFFFFFFF);
            super.render(gfx, mouseX, mouseY, partialTick);
            return;
        }

        // Título
        gfx.drawCenteredString(this.font, "§c⚡ WEREWOLF ⚡", left + WIDTH/2, top + 8, COLOR_TITLE);

        // Separador
        gfx.fill(left + 8, top + 18, left + WIDTH - 8, top + 19, COLOR_BORDER);

        // Nivel y puntos
        gfx.drawString(this.font, "§7Level  §f" + cap.getLevel() + " §7/ 20",
                left + 12, top + 24, COLOR_TEXT, false);
        String pts = "Points: §f" + cap.getAvailableAttributePoints();
        gfx.drawString(this.font, pts,
                left + WIDTH - 12 - this.font.width("Points: " + cap.getAvailableAttributePoints()) - 4,
                top + 24, COLOR_POINTS, false);

        // Barra XP
        int xpBarX = left + 12;
        int xpBarY = top + 37;
        int xpBarW = WIDTH - 24;
        int xpBarH = 8;

        gfx.fill(xpBarX, xpBarY, xpBarX + xpBarW, xpBarY + xpBarH, COLOR_XP_BG);
        float xpRatio = cap.getLevel() >= 20 ? 1.0f
                : (float) cap.getExperience() / cap.expNeededForNextLevel();
        int xpFill = (int)(xpBarW * xpRatio);
        if (xpFill > 0)
            gfx.fill(xpBarX, xpBarY, xpBarX + xpFill, xpBarY + xpBarH, COLOR_XP_FILL);

        String xpText = cap.getLevel() >= 20 ? "MAX"
                : cap.getExperience() + " / " + cap.expNeededForNextLevel() + " XP";
        gfx.drawCenteredString(this.font, "§7" + xpText, left + WIDTH/2, xpBarY + 1, COLOR_TEXT_DIM);

        // Separador
        gfx.fill(left + 8, top + 49, left + WIDTH - 8, top + 50, COLOR_BORDER);

        // Cabecera
        gfx.drawString(this.font, "§7Attribute", left + 12, top + 55, COLOR_TEXT_DIM, false);
        gfx.drawString(this.font, "§7Level",     left + WIDTH - 80, top + 55, COLOR_TEXT_DIM, false);
        gfx.fill(left + 8, top + 65, left + WIDTH - 8, top + 66, 0xFF2A1010);

        // Atributos
        WereAttribute[] attrs = WereAttribute.values();
        int startY = top + 72;
        int rowHeight = 16;

        for (int i = 0; i < attrs.length; i++) {
            WereAttribute attr = attrs[i];
            int rowY = startY + i * rowHeight;
            int attrLevel = cap.getAttributeLevel(attr);
            int maxLevel  = attr.getMaxLevel();

            // Hover
            if (mouseX >= left + 8 && mouseX <= left + WIDTH - 8
                    && mouseY >= rowY - 3 && mouseY <= rowY + rowHeight - 4) {
                gfx.fill(left + 8, rowY - 3, left + WIDTH - 8, rowY + rowHeight - 4, COLOR_ATTR_HOVER);
            }

            // Nombre
            gfx.drawString(this.font, capitalize(attr.name()), left + 14, rowY, COLOR_TEXT, false);

            // Puntos (círculos)
            int dotStartX = left + WIDTH - 84;
            int dotSize = 5;
            int dotGap  = 3;
            for (int d = 0; d < maxLevel; d++) {
                int dotX = dotStartX + d * (dotSize + dotGap);
                gfx.fill(dotX, rowY, dotX + dotSize, rowY + dotSize,
                        d < attrLevel ? COLOR_DOT_FILLED : COLOR_DOT_EMPTY);
            }
        }

        // Separador inferior
        int footerY = startY + attrs.length * rowHeight + 4;
        gfx.fill(left + 8, footerY, left + WIDTH - 8, footerY + 1, COLOR_BORDER);

        // Footer
        String status = cap.isTransformed() ? "§cTransformed" : "§7Human form";
        gfx.drawCenteredString(this.font, status, left + WIDTH/2, footerY + 6, COLOR_TEXT_DIM);

        super.render(gfx, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }

    private WerewolfCapability getCapability() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return null;
        WerewolfCapability cap = mc.player.getData(WerewolfAttachment.WEREWOLF_DATA);
        if (!cap.isWerewolf()) return null;
        return cap;
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.charAt(0) + s.substring(1).toLowerCase();
    }
}