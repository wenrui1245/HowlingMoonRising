// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon.network;

import com.howlingmoon.HowlingMoon;
import com.howlingmoon.WereAbility;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AbilityCooldownPacket(WereAbility ability, int cooldownTicks) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<AbilityCooldownPacket> TYPE =
            new CustomPacketPayload.Type<>(
                    ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "ability_cooldown")
            );

    public static final StreamCodec<FriendlyByteBuf, AbilityCooldownPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, packet) -> {
                        buf.writeEnum(packet.ability());
                        buf.writeInt(packet.cooldownTicks());
                    },
                    buf -> new AbilityCooldownPacket(buf.readEnum(WereAbility.class), buf.readInt())
            );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(AbilityCooldownPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            // This will be handled on the client in Phase 3
            // com.howlingmoon.client.ClientAbilityData.setCooldown(packet.ability(), packet.cooldownTicks());
        });
    }
}
