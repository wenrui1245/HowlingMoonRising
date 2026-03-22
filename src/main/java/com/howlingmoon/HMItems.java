// Copyright (c) 2026 mareca1202. All Rights Reserved.
package com.howlingmoon;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class HMItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(HowlingMoon.MODID);

    public static final DeferredItem<Item> MOON_PEARL =
            ITEMS.registerSimpleItem("moon_pearl", new Item.Properties());

    public static final DeferredItem<Item> SILVER_INGOT =
            ITEMS.registerSimpleItem("silver_ingot", new Item.Properties());

    public static final DeferredItem<ItemWolfsbanePotion> WOLFSBANE_POTION =
            ITEMS.register("wolfsbane_potion", ItemWolfsbanePotion::new);

    public static final DeferredItem<SwordItem> SILVER_SWORD =
            ITEMS.register("silver_sword", () -> new SwordItem(
                    Tiers.IRON,
                    new Item.Properties().attributes(
                            SwordItem.createAttributes(Tiers.IRON, 3, -2.4f)
                    )
            ));
}