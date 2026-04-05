package com.penguin.nuclide.content.multiblocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public final class VerticalTankValidator implements MultiBlockValidator {
    private final Block controllerBlock;
    private final Block casingBlock;
    private final int minHeight;
    private final int maxHeight;

    public VerticalTankValidator(
            Block controllerBlock,
            Block casingBlock,
            int minHeight,
            int maxHeight
    ) {
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

    @Override
    public MultiBlockStructure validate(World world, BlockPos origin) {
        Objects.requireNonNull(world, "world");
        Objects.requireNonNull(origin, "origin");

        if (!world.getBlockState(origin).isOf(controllerBlock)) {
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

                    boolean isControllerPos = (dx == 0 && y == 0 && dz == 0);

                    if (isControllerPos) {
                        if (!state.isOf(controllerBlock)) {
                            layerValid = false;
                            break;
                        }
                    } else {
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
}