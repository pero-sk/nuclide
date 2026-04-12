package com.penguin.nuclide.content.registry;

import com.penguin.nuclide.Nuclide;
import com.penguin.nuclide.content.items.CanisterItem;
import com.penguin.nuclide.content.items.SpeciesFilterItem;

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

    public static final BlockItem HAND_CRANK_BLOCKITEM =
        new BlockItem(ModBlocks.HAND_CRANK, new Item.Settings());

    public static final BlockItem ELECTROLYSER_BLOCKITEM =
        new BlockItem(ModBlocks.ELECTROLYSER, new Item.Settings());

    public static final BlockItem HYDROGEN_FURNACE_BLOCKITEM =
        new BlockItem(ModBlocks.HYDROGEN_FURNACE, new Item.Settings());

    public static final BlockItem CABLE =
        new BlockItem(ModBlocks.CABLE, new Item.Settings());

    public static final BlockItem BINDER_BLOCKITEM =
        new BlockItem(ModBlocks.BINDER, new Item.Settings());

    public static final SpeciesFilterItem SPECIES_FILTER_ITEM =
        new SpeciesFilterItem(new Item.Settings());

    public static final BlockItem CREATIVE_ENERGISER_BLOCKITEM =
        new BlockItem(ModBlocks.CREATIVE_ENERGISER, new Item.Settings());

    private ModItems() {}

    public static void register() {
        reg_item(Nuclide.asIdentifier("canister"), CANISTER_ITEM);
        reg_item(Nuclide.asIdentifier("species_tank"), SPECIESTANK_BLOCKITEM);
        reg_item(Nuclide.asIdentifier("pump"), PUMP_BLOCKITEM);
        reg_item(Nuclide.asIdentifier("pipe"), PIPE_BLOCKITEM);
        reg_item(Nuclide.asIdentifier("hand_crank"), HAND_CRANK_BLOCKITEM);
        reg_item(Nuclide.asIdentifier("electrolyser"), ELECTROLYSER_BLOCKITEM);
        reg_item(Nuclide.asIdentifier("hydrogen_furnace"), HYDROGEN_FURNACE_BLOCKITEM);
        reg_item(Nuclide.asIdentifier("cable"), CABLE);
        reg_item(Nuclide.asIdentifier("species_filter"), SPECIES_FILTER_ITEM);
        reg_item(Nuclide.asIdentifier("binder"), BINDER_BLOCKITEM);
        reg_item(Nuclide.asIdentifier("creative_energiser"), CREATIVE_ENERGISER_BLOCKITEM);
    }

    private static void reg_item(Identifier id, Item item) {
        Registry.register(Registries.ITEM, id, item);
    }
}
