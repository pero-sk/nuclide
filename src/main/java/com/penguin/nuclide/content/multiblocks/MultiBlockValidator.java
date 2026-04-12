package com.penguin.nuclide.content.multiblocks;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface MultiBlockValidator {
    MultiBlockStructure validate(World world, BlockPos origin);
}