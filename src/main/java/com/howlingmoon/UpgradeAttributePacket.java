// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpgradeAttributePacket(String attributeName) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UpgradeAttributePacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "upgrade_attribute")
            );

    public static final StreamCodec<FriendlyByteBuf, UpgradeAttributePacket> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.STRING_UTF8, UpgradeAttributePacket::attributeName,
                    UpgradeAttributePacket::new
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UpgradeAttributePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player)) return;

            WerewolfCapability cap = player.getData(WerewolfAttachment.WEREWOLF_DATA);
            if (!cap.isWerewolf()) return;
            if (cap.getAvailableAttributePoints() <= 0) return;

            for (WereAttribute attr : WereAttribute.values()) {
                if (attr.name().equalsIgnoreCase(packet.attributeName())) {
                    if (cap.canUpgradeAttribute(attr)) {
                        cap.upgradeAttribute(attr);
                        if (cap.isTransformed()) {
                            WerewolfAttributeHandler.applyAllModifiers(player, cap);
                        }
                        PacketDistributor.sendToPlayer(player, SyncWerewolfPacket.fromCap(cap));
                    }
                    break;
                }
            }
        });
    }
}