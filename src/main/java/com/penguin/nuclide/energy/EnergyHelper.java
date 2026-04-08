package com.penguin.nuclide.energy;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public final class EnergyHelper {

    private EnergyHelper() {}

    public static int transfer(World world, BlockPos fromPos, Direction outSide, int maxAmount) {
        BlockPos toPos = fromPos.offset(outSide);

        EnergyNode source = find(world, fromPos);
        EnergyNode target = find(world, toPos);

        if (source == null || target == null) {
            return 0;
        }

        Direction sourceSide = outSide;
        Direction targetSide = outSide.getOpposite();

        if (!source.canExtractEnergy(sourceSide) || !target.canInsertEnergy(targetSide)) {
            return 0;
        }

        int simulated = source.extractEnergy(sourceSide, maxAmount, true);
        if (simulated <= 0) {
            return 0;
        }

        int accepted = target.insertEnergy(targetSide, simulated, true);
        if (accepted <= 0) {
            return 0;
        }

        int extracted = source.extractEnergy(sourceSide, accepted, false);
        if (extracted <= 0) {
            return 0;
        }

        int inserted = target.insertEnergy(targetSide, extracted, false);
        if (inserted != extracted) {
            throw new IllegalStateException("Energy desync: extracted " + extracted + " but inserted " + inserted);
        }

        return inserted;
    }

    public static @Nullable EnergyNode find(World world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof EnergyNode node) {
            return node;
        }
        return null;
    }
}