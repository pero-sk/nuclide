package com.penguin.nuclide.content.blockentities.pump;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.penguin.nuclide.Nuclide;
import com.penguin.nuclide.content.blocks.gas.GasBlock;
import com.penguin.nuclide.content.blocks.pump.PumpBlock;
import com.penguin.nuclide.content.registry.ModBlockEntities;
import com.penguin.nuclide.data.NuclideDataLoader;
import com.penguin.nuclide.data.SpeciesDefinition;
import com.penguin.nuclide.species.SpeciesStack;
import com.penguin.nuclide.transport.SpeciesTransportNode;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class PumpBlockEntity extends BlockEntity {
    private static final int TRANSFER_RATE = Nuclide.DEFAULT_TRANSFER_RATE;

    public PumpBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PUMP, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, PumpBlockEntity blockEntity) {
        if (world.isClient) {
            return;
        }

        if (!state.contains(PumpBlock.FACING)) {
            return;
        }

        Direction facing = state.get(PumpBlock.FACING);

        boolean moved = blockEntity.tryPump(world, pos, facing, TRANSFER_RATE);
        if (!moved) {
            blockEntity.tryPumpFromWorld(world, pos, facing);
        }
    }

    private boolean tryPump(World world, BlockPos pos, Direction facing, int rate) {
        BlockPos sourcePos = pos.offset(facing.getOpposite());
        BlockPos targetPos = pos.offset(facing);

        SpeciesTransportNode source = findTransportNode(world, sourcePos);
        SpeciesTransportNode target = findTransportNode(world, targetPos);

        if (source == null || target == null) {
            return false;
        }

        Direction sourceSide = facing;
        Direction targetSide = facing.getOpposite();

        if (!source.canExtract(sourceSide) || !target.canInsert(targetSide)) {
            return false;
        }

        boolean movedAnything = false;
        List<SpeciesStack> availableStacks = source.getAvailableSpecies(sourceSide);

        for (SpeciesStack stack : availableStacks) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }

            int toMove = Math.min(stack.count(), rate);

            SpeciesStack simulated = source.extractSpecies(
                    sourceSide,
                    s -> s.speciesId().equals(stack.speciesId()),
                    toMove,
                    true
            );

            if (simulated == null || simulated.isEmpty()) {
                continue;
            }

            int accepted = target.insertSpecies(targetSide, simulated, true);
            if (accepted <= 0) {
                continue;
            }

            SpeciesStack extracted = source.extractSpecies(
                    sourceSide,
                    s -> s.speciesId().equals(stack.speciesId()),
                    accepted,
                    false
            );

            if (extracted == null || extracted.isEmpty()) {
                continue;
            }

            int inserted = target.insertSpecies(targetSide, extracted, false);

            if (inserted != extracted.count()) {
                throw new IllegalStateException(
                        "Pump desync: extracted " + extracted.count()
                                + " of " + extracted.speciesId()
                                + " but inserted " + inserted
                );
            }

            movedAnything = true;
        }

        if (movedAnything) {
            markDirty();

            BlockEntity sourceBe = world.getBlockEntity(sourcePos);
            if (sourceBe != null) {
                sourceBe.markDirty();
            }

            BlockEntity targetBe = world.getBlockEntity(targetPos);
            if (targetBe != null) {
                targetBe.markDirty();
            }
        }

        return movedAnything;
    }

    private boolean tryPumpFromWorld(World world, BlockPos pos, Direction facing) {
        BlockPos intakePos = pos.offset(facing.getOpposite());
        BlockPos targetPos = pos.offset(facing);

        SpeciesTransportNode target = findTransportNode(world, targetPos);
        if (target == null) {
            return false;
        }

        Direction targetSide = facing.getOpposite();
        if (!target.canInsert(targetSide)) {
            return false;
        }

        BlockState intakeState = world.getBlockState(intakePos);
        Block intakeBlock = intakeState.getBlock();

        boolean isGas = intakeBlock instanceof GasBlock;
        boolean isLiquid = intakeBlock instanceof FluidBlock;

        if (!isGas && !isLiquid) {
            return false;
        }

        String matchedSpeciesId = findMatchingSpeciesId(intakeBlock, isLiquid, isGas);
        if (matchedSpeciesId == null) {
            return false;
        }

        SpeciesStack stack = new SpeciesStack(matchedSpeciesId, Nuclide.MMOL_PER_BLOCK);
        int accepted = target.insertSpecies(targetSide, stack, true);

        if (accepted < Nuclide.MMOL_PER_BLOCK) {
            return false;
        }

        world.setBlockState(intakePos, Blocks.AIR.getDefaultState());
        target.insertSpecies(targetSide, stack, false);

        markDirty();

        BlockEntity targetBe = world.getBlockEntity(targetPos);
        if (targetBe != null) {
            targetBe.markDirty();
        }

        world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        return true;
    }

    private @Nullable String findMatchingSpeciesId(Block block, boolean liquid, boolean gas) {
        Identifier blockId = Registries.BLOCK.getId(block);
        String blockIdString = blockId.toString();

        for (SpeciesDefinition species : NuclideDataLoader.SPECIES.values()) {
            if (species.representation() == null) {
                continue;
            }

            if (liquid
                    && species.representation().liquid() != null
                    && species.representation().liquid().block() != null
                    && blockIdString.equals(species.representation().liquid().block())) {
                return species.id();
            }

            if (gas
                    && species.representation().gas() != null
                    && species.representation().gas().block() != null
                    && blockIdString.equals(species.representation().gas().block())) {
                return species.id();
            }
        }

        return null;
    }

    private @Nullable SpeciesTransportNode findTransportNode(World world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof SpeciesTransportNode node) {
            return node;
        }
        return null;
    }
}