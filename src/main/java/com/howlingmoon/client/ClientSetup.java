// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon.client;

import com.howlingmoon.HMEntities;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public class ClientSetup {

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // Registramos el renderizado del Hunter
        event.registerEntityRenderer(HMEntities.HUNTER.get(), HunterRenderer::new);
        
        // Registramos el renderizado del Mob Werewolf (si lo usas como NPC)
        event.registerEntityRenderer(HMEntities.WEREWOLF.get(), WerewolfGeoRenderer::new);
    }
    
    // Eliminamos registerLayerDefinitions y addPlayerLayers
    // porque GeckoLib se encargará de interceptar al jugador mediante eventos.
}