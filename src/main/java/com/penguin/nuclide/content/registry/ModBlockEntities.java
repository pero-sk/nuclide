package com.penguin.nuclide.content.registry;

import com.penguin.nuclide.Nuclide;
import com.penguin.nuclide.content.blockentities.binder.BinderBlockEntity;
import com.penguin.nuclide.content.blockentities.cable.CableBlockEntity;
import com.penguin.nuclide.content.blockentities.creative.energiser.CreativeEnergiserBlockEntity;
import com.penguin.nuclide.content.blockentities.electrolyser.ElectrolyserBlockEntity;
import com.penguin.nuclide.content.blockentities.hand_crank.HandCrankBlockEntity;
import com.penguin.nuclide.content.blockentities.hydrogen_furnace.HydrogenFurnaceBlockEntity;
import com.penguin.nuclide.content.blockentities.pipe.PipeBlockEntity;
import com.penguin.nuclide.content.blockentities.pump.PumpBlockEntity;
import com.penguin.nuclide.content.blockentities.species_tank.SpeciesTankCasingBlockEntity;
import com.penguin.nuclide.content.blockentities.species_tank.SpeciesTankControllerBlockEntity;
import com.penguin.nuclide.content.blockentities.vent.VentBlockEntity;

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

        public static final BlockEntityType<HandCrankBlockEntity> HAND_CRANK =
                Registry.register(
                        Registries.BLOCK_ENTITY_TYPE,
                        Nuclide.asIdentifier("hand_crank"),
                        BlockEntityType.Builder
                                .create(HandCrankBlockEntity::new, ModBlocks.HAND_CRANK)
                                .build(null)
                );

        public static final BlockEntityType<HydrogenFurnaceBlockEntity> HYDROGEN_FURNACE =
                Registry.register(
                        Registries.BLOCK_ENTITY_TYPE,
                        Nuclide.asIdentifier("hydrogen_furnace"),
                        BlockEntityType.Builder
                                .create(HydrogenFurnaceBlockEntity::new, ModBlocks.HYDROGEN_FURNACE)
                                .build(null)
                );

        public static final BlockEntityType<CableBlockEntity> CABLE =
                Registry.register(
                        Registries.BLOCK_ENTITY_TYPE,
                        Nuclide.asIdentifier("cable"),
                        BlockEntityType.Builder
                                .create(CableBlockEntity::new, ModBlocks.CABLE)
                                .build(null)
                );

        public static final BlockEntityType<BinderBlockEntity> BINDER =
                Registry.register(
                        Registries.BLOCK_ENTITY_TYPE,
                        Nuclide.asIdentifier("binder"),
                        BlockEntityType.Builder
                                .create(BinderBlockEntity::new, ModBlocks.BINDER)
                                .build(null)
                );

        public static final BlockEntityType<VentBlockEntity> VENT =
                Registry.register(
                        Registries.BLOCK_ENTITY_TYPE,
                        Nuclide.asIdentifier("vent"),
                        BlockEntityType.Builder
                                .create(VentBlockEntity::new, ModBlocks.VENT)
                                .build(null)
                );

        public static final BlockEntityType<CreativeEnergiserBlockEntity> CREATIVE_ENERGISER =
                Registry.register(
                        Registries.BLOCK_ENTITY_TYPE,
                        Nuclide.asIdentifier("creative_energiser"),
                        BlockEntityType.Builder
                                .create(CreativeEnergiserBlockEntity::new, ModBlocks.CREATIVE_ENERGISER)
                                .build(null)
                );

        public static void init() {
        }

        private ModBlockEntities() {}
}