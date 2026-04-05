package com.penguin.nuclide.content.registry;

import com.penguin.nuclide.Nuclide;
import com.penguin.nuclide.content.blocks.ElectrolyserBlock;
import com.penguin.nuclide.content.blocks.GasBlock;
import com.penguin.nuclide.content.blocks.PipeBlock;
import com.penguin.nuclide.content.blocks.PumpBlock;
import com.penguin.nuclide.content.blocks.SpeciesTankCasingBlock;
import com.penguin.nuclide.content.blocks.SpeciesTankControllerBlock;
import com.penguin.nuclide.content.blocks.VentBlock;
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
    );

    public static final ElectrolyserBlock ELECTROLYSER = new ElectrolyserBlock(
            AbstractBlock.Settings.create()
                    .strength(2.0f)
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

        Nuclide.GASES.register(HYDROGEN_GAS_TYPE);
    }
}