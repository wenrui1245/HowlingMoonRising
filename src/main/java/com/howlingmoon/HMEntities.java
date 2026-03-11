package com.howlingmoon;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;

@EventBusSubscriber(modid = HowlingMoon.MODID, bus = EventBusSubscriber.Bus.MOD)
public class HMEntities {

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(Registries.ENTITY_TYPE, HowlingMoon.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<WerewolfEntity>> WEREWOLF =
            ENTITIES.register("werewolf", () -> EntityType.Builder
                    .<WerewolfEntity>of(WerewolfEntity::new, MobCategory.MONSTER)
                    .sized(0.8f, 2.5f)
                    .build("werewolf"));

    @SubscribeEvent
    public static void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(WEREWOLF.get(), WerewolfEntity.createAttributes().build());
    }
}