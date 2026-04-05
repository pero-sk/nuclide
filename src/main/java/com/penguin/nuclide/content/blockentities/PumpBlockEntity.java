package com.penguin.nuclide.content.blockentities;

import org.jetbrains.annotations.Nullable;

import com.penguin.nuclide.content.blocks.PumpBlock;
import com.penguin.nuclide.content.registry.ModBlockEntities;
import com.penguin.nuclide.species.SpeciesStack;
import com.penguin.nuclide.transport.SpeciesFilter;
import com.penguin.nuclide.transport.SpeciesTransportNode;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class PumpBlockEntity extends BlockEntity {
    private static final int TRANSFER_RATE = 100;

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
        blockEntity.tryPump(world, pos, facing, TRANSFER_RATE);
    }

    private void tryPump(World world, BlockPos pos, Direction facing, int rate) {
        BlockPos sourcePos = pos.offset(facing.getOpposite());
        BlockPos targetPos = pos.offset(facing);

        SpeciesTransportNode source = findTransportNode(world, sourcePos);
        SpeciesTransportNode target = findTransportNode(world, targetPos);

        if (source == null || target == null) {
            return;
        }

        Direction sourceSide = facing;
        Direction targetSide = facing.getOpposite();

        if (!source.canExtract(sourceSide) || !target.canInsert(targetSide)) {
            return;
        }

        SpeciesStack simulated = source.extractSpecies(sourceSide, SpeciesFilter.any(), rate, true);
        if (simulated == null || simulated.isEmpty()) {
            return;
        }

        int accepted = target.insertSpecies(targetSide, simulated, true);
        if (accepted <= 0) {
            return;
        }

        SpeciesStack extracted = source.extractSpecies(sourceSide, SpeciesFilter.any(), accepted, false);
        if (extracted == null || extracted.isEmpty()) {
            return;
        }

        int inserted = target.insertSpecies(targetSide, extracted, false);
        if (inserted != extracted.count()) {
            throw new IllegalStateException(
                    "Pump desync: extracted " + extracted.count() + " of " + extracted.speciesId()
                            + " but inserted " + inserted
            );
        }

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

    private @Nullable SpeciesTransportNode findTransportNode(World world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof SpeciesTransportNode node) {
            return node;
        }
        return null;
    }
}