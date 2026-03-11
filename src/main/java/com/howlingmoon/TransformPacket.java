// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record TransformPacket() implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<TransformPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "transform")
            );

    public static final StreamCodec<FriendlyByteBuf, TransformPacket> STREAM_CODEC =
            StreamCodec.unit(new TransformPacket());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(TransformPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;
            WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
            if (!cap.isWerewolf()) return;
            cap.setTransformed(!cap.isTransformed());
            WerewolfCommand.syncToClient(player, cap);
        });
    }
}