package com.penguin.nuclide.content.multiblocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class VerticalTankFinder {
    private final Block controllerBlock;
    private final Block casingBlock;
    private final int minHeight;
    private final int maxHeight;

    public VerticalTankFinder(Block controllerBlock, Block casingBlock, int minHeight, int maxHeight) {
        this.controllerBlock = Objects.requireNonNull(controllerBlock, "controllerBlock");
        this.casingBlock = Objects.requireNonNull(casingBlock, "casingBlock");
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;

        if (minHeight <= 0) {
            throw new IllegalArgumentException("minHeight must be > 0");
        }
        if (maxHeight < minHeight) {
            throw new IllegalArgumentException("maxHeight must be >= minHeight");
        }
    }

    public MultiBlockStructure findFromAnyMember(World world, BlockPos pos) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(pos, "pos");

        for (int dx = 0; dx <= 1; dx++) {
            for (int dz = 0; dz <= 1; dz++) {
                BlockPos candidateOrigin = pos.add(-dx, 0, -dz);
                MultiBlockStructure structure = validateCandidate(world, candidateOrigin);
                if (structure != null) {
                    return structure;
                }
            }
        }

        return null;
    }

    private MultiBlockStructure validateCandidate(World world, BlockPos origin) {
        // Enforce bottom-most-ness:
        // if any of the 2x2 base cells has a tank block directly below it,
        // this cannot be the true base origin.
        if (hasTankBlockBelowBase(world, origin)) {
            return null;
        }

        int validHeight = 0;
        List<BlockPos> members = new ArrayList<>(4 * maxHeight);

        for (int y = 0; y < maxHeight; y++) {
            List<BlockPos> layerMembers = new ArrayList<>(4);
            boolean layerValid = true;

            for (int dx = 0; dx < 2 && layerValid; dx++) {
                for (int dz = 0; dz < 2; dz++) {
                    BlockPos current = origin.add(dx, y, dz);
                    BlockState state = world.getBlockState(current);

                    boolean isBaseOrigin = (dx == 0 && y == 0 && dz == 0);

                    if (isBaseOrigin) {
                        // During discovery, the base origin may be either casing or controller.
                        if (!isTankBlock(state)) {
                            layerValid = false;
                            break;
                        }
                    } else {
                        // Every other block must be casing only.
                        // This prevents extra controllers in upper layers or side positions.
                        if (!state.isOf(casingBlock)) {
                            layerValid = false;
                            break;
                        }
                    }

                    layerMembers.add(current);
                }
            }

            if (!layerValid) {
                break;
            }

            members.addAll(layerMembers);
            validHeight++;
        }

        if (validHeight < minHeight) {
            return null;
        }

        return new MultiBlockStructure(origin, 2, validHeight, 2, members);
    }

    private boolean hasTankBlockBelowBase(World world, BlockPos origin) {
        for (int dx = 0; dx < 2; dx++) {
            for (int dz = 0; dz < 2; dz++) {
                BlockPos below = origin.add(dx, -1, dz);
                if (isTankBlock(world.getBlockState(below))) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isTankBlock(BlockState state) {
        return state.isOf(controllerBlock) || state.isOf(casingBlock);
    }
}