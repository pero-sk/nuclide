package com.penguin.nuclide.content.registry;

import com.penguin.nuclide.Nuclide;
import com.penguin.nuclide.content.blocks.binder.BinderBlock;
import com.penguin.nuclide.content.blocks.cable.CableBlock;
import com.penguin.nuclide.content.blocks.creative.energiser.CreativeEnergiserBlock;
import com.penguin.nuclide.content.blocks.electrolyser.ElectrolyserBlock;
import com.penguin.nuclide.content.blocks.gas.GasBlock;
import com.penguin.nuclide.content.blocks.hand_crank.HandCrankBlock;
import com.penguin.nuclide.content.blocks.hydrogen_furnace.HydrogenFurnaceBlock;
import com.penguin.nuclide.content.blocks.pipe.PipeBlock;
import com.penguin.nuclide.content.blocks.pump.PumpBlock;
import com.penguin.nuclide.content.blocks.species_tank.SpeciesTankCasingBlock;
import com.penguin.nuclide.content.blocks.species_tank.SpeciesTankControllerBlock;
import com.penguin.nuclide.content.blocks.vent.VentBlock;
import com.penguin.nuclide.gas.GasType;

import net.minecraft.block.AbstractBlock;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModBlocks {

    public static final GasType HYDROGEN_GAS_TYPE = new GasType(
        Nuclide.asIdentifier("hydrogen_gas"),
        0.07f,
        1.0f,
        1.2f,
        GasType.COLORLESS,
        "Hydrogen Gas",
        1.0f,
        0.0f
    );

    public static final GasBlock HYDROGEN_GAS = new GasBlock(
        AbstractBlock.Settings.create()
                .noCollision()
                .nonOpaque()
                .replaceable()
                .strength(0.0f)
                .dropsNothing(),
        HYDROGEN_GAS_TYPE
    );


    public static final VentBlock VENT = new VentBlock(
        AbstractBlock.Settings.create()
                .nonOpaque()
                .strength(0.0f)
                .dropsNothing()
    );

    public static final SpeciesTankCasingBlock SPECIES_TANK_CASING = new SpeciesTankCasingBlock(
        AbstractBlock.Settings.create()
                .strength(2.0f)
    );

    public static final SpeciesTankControllerBlock SPECIES_TANK_CONTROLLER = new SpeciesTankControllerBlock(
        AbstractBlock.Settings.create()
                .strength(2.0f)
    );

    public static final PumpBlock PUMP = new PumpBlock(
        AbstractBlock.Settings.create()
                .strength(2.0f)
    );

    public static final PipeBlock PIPE = new PipeBlock(
        AbstractBlock.Settings.create()
                .strength(2.0f)
                .nonOpaque()
    );

    public static final ElectrolyserBlock ELECTROLYSER = new ElectrolyserBlock(
        AbstractBlock.Settings.create()
                .strength(2.0f)
    );

    public static final HandCrankBlock HAND_CRANK = new HandCrankBlock(
        AbstractBlock.Settings.create()
                .strength(0.5f)
    );

    public static final HydrogenFurnaceBlock HYDROGEN_FURNACE = new HydrogenFurnaceBlock(
        AbstractBlock.Settings.create()
                .strength(2.0f)
    );

    public static final CableBlock CABLE = new CableBlock(
        AbstractBlock.Settings.create()
                .strength(0.5f)
    );

    public static final BinderBlock BINDER = new BinderBlock(
        AbstractBlock.Settings.create()
                .strength(3.5f)
    );

    public static final CreativeEnergiserBlock CREATIVE_ENERGISER = new CreativeEnergiserBlock(
        AbstractBlock.Settings.create()
                .strength(1.0f)
    );

    private ModBlocks() {}

    public static void register() {
        Registry.register(Registries.BLOCK, Nuclide.asIdentifier("hydrogen_gas"), HYDROGEN_GAS);
        Registry.register(Registries.BLOCK, Nuclide.asIdentifier("vent"), VENT);
        Registry.register(Registries.BLOCK, Nuclide.asIdentifier("species_tank_casing"), SPECIES_TANK_CASING);
        Registry.register(Registries.BLOCK, Nuclide.asIdentifier("species_tank_controller"), SPECIES_TANK_CONTROLLER);
        Registry.register(Registries.BLOCK, Nuclide.asIdentifier("pump"), PUMP);
        Registry.register(Registries.BLOCK, Nuclide.asIdentifier("pipe"), PIPE);
        Registry.register(Registries.BLOCK, Nuclide.asIdentifier("electrolyser"), ELECTROLYSER);
        Registry.register(Registries.BLOCK, Nuclide.asIdentifier("hand_crank"), HAND_CRANK);
        Registry.register(Registries.BLOCK, Nuclide.asIdentifier("hydrogen_furnace"), HYDROGEN_FURNACE);
        Registry.register(Registries.BLOCK, Nuclide.asIdentifier("cable"), CABLE);
        Registry.register(Registries.BLOCK, Nuclide.asIdentifier("binder"), BINDER);
        Registry.register(Registries.BLOCK, Nuclide.asIdentifier("creative_energiser"), CREATIVE_ENERGISER);

        Nuclide.GASES.register(HYDROGEN_GAS_TYPE);
    }
}