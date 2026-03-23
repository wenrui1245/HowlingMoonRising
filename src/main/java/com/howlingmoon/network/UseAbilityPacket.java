// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon.network;

import com.howlingmoon.HowlingMoon;
import com.howlingmoon.WereAbility;
import com.howlingmoon.WerewolfAbilityHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UseAbilityPacket(WereAbility ability) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<UseAbilityPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "use_ability")
            );

    public static final StreamCodec<FriendlyByteBuf, UseAbilityPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> buf.writeEnum(packet.ability()),
                    buf -> new UseAbilityPacket(buf.readEnum(WereAbility.class))
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(UseAbilityPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            WerewolfAbilityHandler.handleAbilityUse((ServerPlayer) context.player(), packet.ability());
        });
    }
}
