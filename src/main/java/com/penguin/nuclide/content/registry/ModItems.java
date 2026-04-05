package com.penguin.nuclide.content.registry;

import com.penguin.nuclide.Nuclide;
import com.penguin.nuclide.content.items.CanisterItem;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModItems {
    
    public static final CanisterItem CANISTER_ITEM =
        new CanisterItem(new Item.Settings().maxCount(1));

    public static final BlockItem SPECIESTANK_BLOCKITEM =
        new BlockItem(ModBlocks.SPECIES_TANK_CASING, new Item.Settings());

    public static final BlockItem PUMP_BLOCKITEM =
        new BlockItem(ModBlocks.PUMP, new Item.Settings());

    public static final BlockItem PIPE_BLOCKITEM =
        new BlockItem(ModBlocks.PIPE, new Item.Settings());

    private ModItems() {}

    public static void register() {
        reg_item(Nuclide.asIdentifier("canister_item"), CANISTER_ITEM);
        reg_item(Nuclide.asIdentifier("species_tank"), SPECIESTANK_BLOCKITEM);
        reg_item(Nuclide.asIdentifier("pump"), PUMP_BLOCKITEM);
        reg_item(Nuclide.asIdentifier("pipe"), PIPE_BLOCKITEM);
    }

    private static void reg_item(Identifier id, Item item) {
        Registry.register(Registries.ITEM, id, item);
    }
}
