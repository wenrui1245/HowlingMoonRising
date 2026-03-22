// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.network.FriendlyByteBuf;
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
        Map<String, Integer> attributeTree,
        boolean moonForced
) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<SyncWerewolfPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "sync_werewolf")
            );

    public static final StreamCodec<FriendlyByteBuf, SyncWerewolfPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> {
                        buf.writeBoolean(packet.isWerewolf());
                        buf.writeBoolean(packet.isTransformed());
                        buf.writeInt(packet.level());
                        buf.writeInt(packet.experience());
                        buf.writeInt(packet.usedAttributePoints());
                        Map<String, Integer> tree = packet.attributeTree();
                        buf.writeInt(tree.size());
                        tree.forEach((k, v) -> {
                            buf.writeUtf(k);
                            buf.writeInt(v);
                        });
                        buf.writeBoolean(packet.moonForced());
                    },
                    buf -> {
                        boolean isWerewolf = buf.readBoolean();
                        boolean isTransformed = buf.readBoolean();
                        int level = buf.readInt();
                        int experience = buf.readInt();
                        int usedAttributePoints = buf.readInt();
                        int size = buf.readInt();
                        Map<String, Integer> tree = new HashMap<>();
                        for (int i = 0; i < size; i++) {
                            tree.put(buf.readUtf(), buf.readInt());
                        }
                        boolean moonForced = buf.readBoolean();
                        return new SyncWerewolfPacket(
                                isWerewolf, isTransformed, level, experience,
                                usedAttributePoints, tree, moonForced
                        );
                    }
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
                cap.setMoonForced(packet.moonForced());
                cap.setLevel(packet.level());
                cap.setExperience(packet.experience());
                cap.setUsedAttributePoints(packet.usedAttributePoints());
                cap.setAttributeTree(packet.attributeTree());
            }
        });
    }
}