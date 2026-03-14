// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon.client;

import com.howlingmoon.WerewolfPlayerRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class WerewolfPartsModel extends Model {

    private final ModelPart snout;
    private final ModelPart tail;

    public WerewolfPartsModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.snout = root.getChild("snout");
        this.tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Hocico
        root.addOrReplaceChild("snout", CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-2.0f, -3.0f, -7.0f, 4, 4, 3),
                PartPose.offset(0.0f, 0.0f, 0.0f));

        // Cola — justo encima de las piernas, parte baja del cuerpo
        root.addOrReplaceChild("tail", CubeListBuilder.create()
                        .texOffs(32, 0)
                        .addBox(-1.0f, 0.0f, 0.0f, 2, 7, 2),
                PartPose.offsetAndRotation(0.0f, 10.0f, 2.0f,
                        0.5f, 0.0f, 0.0f));

        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer,
                               int packedLight, int packedOverlay, int color) {
        snout.render(poseStack, buffer, packedLight, packedOverlay);
        tail.render(poseStack, buffer, packedLight, packedOverlay);
    }

    public void renderSnout(PoseStack poseStack, MultiBufferSource buffer,
                            int packedLight, int packedOverlay) {
        var vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(
                WerewolfPlayerRenderer.WEREWOLF_SKIN));
        snout.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }

    public void renderTail(PoseStack poseStack, MultiBufferSource buffer,
                           int packedLight, int packedOverlay) {
        var vertexConsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(
                WerewolfPlayerRenderer.WEREWOLF_SKIN));
        tail.render(poseStack, vertexConsumer, packedLight, packedOverlay);
    }

    public void animateTail(float ageInTicks, float limbSwing, float limbSwingAmount) {
        tail.xRot = 0.5f + (float) Math.sin(ageInTicks * 0.15f) * 0.15f;
        tail.yRot = (float) Math.sin(limbSwing * 0.5f) * limbSwingAmount * 0.4f;
    }
}