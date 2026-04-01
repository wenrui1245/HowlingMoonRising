// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon.client;

import com.howlingmoon.WerewolfAttachment;
import com.howlingmoon.WerewolfCapability;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ScentTrackingRenderer {

    private static class ScentPoint {
        Vec3 pos;
        int age;
        int maxAge;
        float r, g, b;

        ScentPoint(Vec3 pos, int maxAge, float r, float g, float b) {
            this.pos = pos;
            this.age = 0;
            this.maxAge = maxAge;
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }

    private static final List<ScentPoint> scentPoints = new ArrayList<>();
    private static int activationTimer = 0;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused())
            return;

        WerewolfCapability cap = mc.player.getData(WerewolfAttachment.WEREWOLF_DATA);

        // La habilidad está activa SOLAMENTE si el jugador tiene nuestro marcador de
        // "Suerte"
        // y además está transformado en lobo.
        boolean isTracking = cap.isTransformed() && mc.player.hasEffect(MobEffects.LUCK);

        if (isTracking) {
            activationTimer++;
            // Retraso de 2 segundos (40 ticks) al iniciar la habilidad
            if (activationTimer > 40) {
                // Dejar un rastro cada 10 ticks (0.5s)
                if (mc.player.tickCount % 10 == 0) {
                    double range = 32.0;
                    mc.level.getEntitiesOfClass(LivingEntity.class, mc.player.getBoundingBox().inflate(range),
                            e -> e != mc.player).forEach(entity -> {
                                // Colores: Animal = Verde, Monstruo = Rojo, Otros = Naranja
                                float r = 1.0f, g = 0.5f, b = 0.0f;
                                if (entity instanceof Animal) {
                                    r = 0.1f;
                                    g = 1.0f;
                                    b = 0.1f;
                                }
                                if (entity instanceof Monster) {
                                    r = 1.0f;
                                    g = 0.1f;
                                    b = 0.1f;
                                }

                                scentPoints.add(new ScentPoint(entity.position().add(0, entity.getBbHeight() / 2.0, 0),
                                        200, r, g, b));
                            });
                }
            }
        } else {
            activationTimer = 0;
            scentPoints.clear(); // Limpiamos la pantalla inmediatamente si se apaga
        }

        // Envejecer y limpiar puntos viejos
        Iterator<ScentPoint> it = scentPoints.iterator();
        while (it.hasNext()) {
            ScentPoint p = it.next();
            p.age++;
            if (p.age > p.maxAge)
                it.remove();
        }
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES)
            return;
        if (scentPoints.isEmpty())
            return;

        Minecraft mc = Minecraft.getInstance();
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();
        PoseStack poseStack = event.getPoseStack();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);
        Matrix4f matrix = poseStack.last().pose();

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        // X-RAY: Ver a través de paredes
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.disableCull();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        for (ScentPoint p : scentPoints) {
            float alpha = Math.max(0, 1.0f - ((float) p.age / p.maxAge));
            float size = 0.15f;

            float px = (float) p.pos.x;
            float py = (float) p.pos.y;
            float pz = (float) p.pos.z;

            // Dibujar cuadrado
            buffer.addVertex(matrix, px - size, py, pz).setColor(p.r, p.g, p.b, alpha);
            buffer.addVertex(matrix, px, py - size, pz).setColor(p.r, p.g, p.b, alpha);
            buffer.addVertex(matrix, px + size, py, pz).setColor(p.r, p.g, p.b, alpha);
            buffer.addVertex(matrix, px, py + size, pz).setColor(p.r, p.g, p.b, alpha);
        }

        BufferUploader.drawWithShader(buffer.buildOrThrow());

        RenderSystem.enableCull();
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        poseStack.popPose();
    }
}