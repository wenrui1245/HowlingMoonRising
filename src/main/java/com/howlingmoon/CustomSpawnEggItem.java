// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

import java.util.function.Supplier;

public class CustomSpawnEggItem extends Item {

    private final Supplier<? extends EntityType<?>> entityType;

    public CustomSpawnEggItem(Supplier<? extends EntityType<?>> entityType, Properties properties) {
        super(properties);
        this.entityType = entityType;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!(context.getLevel() instanceof ServerLevel level)) {
            return InteractionResult.SUCCESS;
        }

        BlockPos pos = context.getClickedPos();
        Direction direction = context.getClickedFace();
        BlockPos spawnPos = pos.relative(direction);

        EntityType<?> type = this.entityType.get();
        if (type.spawn(level, context.getItemInHand(), context.getPlayer(),
                spawnPos, MobSpawnType.SPAWN_EGG, true,
                !direction.equals(Direction.UP)) != null) {
            context.getItemInHand().shrink(1);
        }

        return InteractionResult.CONSUME;
    }
}
