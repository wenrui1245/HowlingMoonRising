// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;

public class HMSounds {

    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, HowlingMoon.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> HOWL =
            SOUNDS.register("howl", () ->
                    SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "howl")));

    public static final DeferredHolder<SoundEvent, SoundEvent> HEARTBEAT =
            SOUNDS.register("heartbeat", () ->
                    SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "heartbeat")));

    public static final DeferredHolder<SoundEvent, SoundEvent> HEARTBEAT_DELAY =
            SOUNDS.register("heartbeat_delay", () ->
                    SoundEvent.createVariableRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(HowlingMoon.MODID, "heartbeat_delay")));
}