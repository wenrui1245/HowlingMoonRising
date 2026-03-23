// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon.network;

import com.howlingmoon.HowlingMoon;
import com.howlingmoon.SyncWerewolfPacket;
import com.howlingmoon.WereAbility;
import com.howlingmoon.WerewolfAttachment;
import com.howlingmoon.WerewolfCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UnlockAbilityPacket(WereAbility ability) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UnlockAbilityPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "unlock_ability")
            );

    public static final StreamCodec<FriendlyByteBuf, UnlockAbilityPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> buf.writeEnum(packet.ability()),
                    buf -> new UnlockAbilityPacket(buf.readEnum(WereAbility.class))
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UnlockAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                WerewolfCapability cap = serverPlayer.getData(WerewolfAttachment.WEREWOLF_DATA);
                if (cap.canUnlockAbility(packet.ability())) {
                    cap.unlockAbility(packet.ability());
                    // Sync back to client
                    PacketDistributor.sendToPlayer(serverPlayer, SyncWerewolfPacket.fromCap(cap));
                }
            }
        });
    }
}
