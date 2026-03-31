// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import com.howlingmoon.client.ClientSetup;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

/**
 * Punto de entrada del mod exclusivo del cliente.
 */
@Mod(value = HowlingMoon.MODID, dist = Dist.CLIENT)
public class HowlingMoonClient {

    public HowlingMoonClient(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(ClientSetup::registerRenderers);
        modEventBus.addListener(WerewolfKeyBindings::onRegisterKeyMappings);
    }
}