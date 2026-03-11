// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;

public record SyncWerewolfPacket(
        boolean isWerewolf,
        boolean isTransformed,
        int level,
        int experience,
        int usedAttributePoints,
        Map<String, Integer> attributeTree
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncWerewolfPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "sync_werewolf")
            );

    // Codec para Map<String, Integer>
    private static final StreamCodec<FriendlyByteBuf, Map<String, Integer>> MAP_CODEC =
            StreamCodec.of(
                    (buf, map) -> {
                        buf.writeInt(map.size());
                        map.forEach((k, v) -> {
                            buf.writeUtf(k);
                            buf.writeInt(v);
                        });
                    },
                    buf -> {
                        int size = buf.readInt();
                        Map<String, Integer> map = new HashMap<>();
                        for (int i = 0; i < size; i++) {
                            map.put(buf.readUtf(), buf.readInt());
                        }
                        return map;
                    }
            );

    public static final StreamCodec<FriendlyByteBuf, SyncWerewolfPacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.BOOL,    SyncWerewolfPacket::isWerewolf,
                    ByteBufCodecs.BOOL,    SyncWerewolfPacket::isTransformed,
                    ByteBufCodecs.INT,     SyncWerewolfPacket::level,
                    ByteBufCodecs.INT,     SyncWerewolfPacket::experience,
                    ByteBufCodecs.INT,     SyncWerewolfPacket::usedAttributePoints,
                    MAP_CODEC,             SyncWerewolfPacket::attributeTree,
                    SyncWerewolfPacket::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(SyncWerewolfPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() != null) {
                WerewolfCapability cap = context.player()
                        .getData(WerewolfAttachment.WEREWOLF_DATA);
                cap.setWerewolf(packet.isWerewolf());
                cap.setTransformed(packet.isTransformed());
                cap.setLevel(packet.level());
                cap.setExperience(packet.experience());
                cap.setUsedAttributePoints(packet.usedAttributePoints());
                cap.setAttributeTree(packet.attributeTree());
            }
        });
    }
}