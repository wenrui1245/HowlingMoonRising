// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;

@EventBusSubscriber(modid = HowlingMoon.MODID, value = Dist.CLIENT)
public class WerewolfPlayerRenderer {

    // Instancia perezosa (lazy) de nuestro renderer de GeckoLib
    private static WerewolfReplacedRenderer replacedRenderer;

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        Player player = event.getEntity();
        if (!player.level().isClientSide()) return;

        WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
        
        if (cap.isTransformed()) {
            // 1. Cancelamos el renderizado de la skin humana
            event.setCanceled(true);

            // 2. Inicializamos el renderer de GeckoLib si no existe
            if (replacedRenderer == null) {
                EntityRendererProvider.Context context = new EntityRendererProvider.Context(
                        Minecraft.getInstance().getEntityRenderDispatcher(),
                        Minecraft.getInstance().getItemRenderer(),
                        Minecraft.getInstance().getBlockRenderer(),
                        Minecraft.getInstance().getEntityRenderDispatcher().getItemInHandRenderer(),
                        Minecraft.getInstance().getResourceManager(),
                        Minecraft.getInstance().getEntityModels(),
                        Minecraft.getInstance().font
                );
                replacedRenderer = new WerewolfReplacedRenderer(context);
            }

            // 3. Actualizamos a quién estamos mirando para las animaciones
            WerewolfReplacedRenderer.currentPlayer = (AbstractClientPlayer) player;

            // 4. Extraemos las rotaciones originales (cabeza, brazos) para que GeckoLib las use
            WerewolfReplacedRenderer.extractVanillaBones((AbstractClientPlayer) player, event.getPartialTick());

            // 5. Preparamos el tamaño y renderizamos el modelo 3D
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            
            // Aumentamos el tamaño como lo tenías antes
            poseStack.scale(1.2f, 1.2f, 1.2f);

            replacedRenderer.render(
                    (AbstractClientPlayer) player,
                    player.getYRot(),
                    event.getPartialTick(),
                    poseStack,
                    event.getMultiBufferSource(),
                    event.getPackedLight()
            );

            poseStack.popPose();
        }
    }
}