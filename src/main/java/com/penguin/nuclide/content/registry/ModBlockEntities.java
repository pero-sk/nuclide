package com.penguin.nuclide.content.registry;

import com.penguin.nuclide.Nuclide;
import com.penguin.nuclide.content.blockentities.ElectrolyserBlockEntity;
import com.penguin.nuclide.content.blockentities.PipeBlockEntity;
import com.penguin.nuclide.content.blockentities.PumpBlockEntity;
import com.penguin.nuclide.content.blockentities.SpeciesTankCasingBlockEntity;
import com.penguin.nuclide.content.blockentities.SpeciesTankControllerBlockEntity;

import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModBlockEntities {

    public static final BlockEntityType<SpeciesTankControllerBlockEntity> SPECIES_TANK_CONTROLLER =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    Nuclide.asIdentifier("species_tank_controller"),
                    BlockEntityType.Builder
                            .create(SpeciesTankControllerBlockEntity::new, ModBlocks.SPECIES_TANK_CONTROLLER)
                            .build(null)
            );

    public static final BlockEntityType<SpeciesTankCasingBlockEntity> SPECIES_TANK_CASING =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    Nuclide.asIdentifier("species_tank_casing"),
                    BlockEntityType.Builder
                            .create(SpeciesTankCasingBlockEntity::new, ModBlocks.SPECIES_TANK_CASING)
                            .build(null)
            );

    public static final BlockEntityType<PumpBlockEntity> PUMP =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    Nuclide.asIdentifier("pump"),
                    BlockEntityType.Builder
                            .create(PumpBlockEntity::new, ModBlocks.PUMP)
                            .build(null)
            );

    public static final BlockEntityType<PipeBlockEntity> PIPE =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    Nuclide.asIdentifier("pipe"),
                    BlockEntityType.Builder
                            .create(PipeBlockEntity::new, ModBlocks.PIPE)
                            .build(null)
            );

    public static final BlockEntityType<ElectrolyserBlockEntity> ELECTROLYSER =
            Registry.register(
                    Registries.BLOCK_ENTITY_TYPE,
                    Nuclide.asIdentifier("electrolyser"),
                    BlockEntityType.Builder
                            .create(ElectrolyserBlockEntity::new, ModBlocks.ELECTROLYSER)
                            .build(null)
            );

    public static void init() {
    }

    private ModBlockEntities() {}
}