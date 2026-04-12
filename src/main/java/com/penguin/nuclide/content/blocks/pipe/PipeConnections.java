package com.penguin.nuclide.content.blocks.pipe;

import com.penguin.nuclide.content.blockentities.pipe.PipeBlockEntity;
import com.penguin.nuclide.content.blockentities.pump.PumpBlockEntity;
import com.penguin.nuclide.content.blocks.pump.PumpBlock;
import com.penguin.nuclide.content.registry.ModBlocks;
import com.penguin.nuclide.transport.SpeciesTransportNode;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockRenderView;
import net.minecraft.world.BlockView;

public final class PipeConnections {

    private PipeConnections() {}

    public static boolean connectsTo(BlockView world, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.offset(direction);
        BlockState neighborState = world.getBlockState(neighborPos);
        BlockEntity neighborBe = world.getBlockEntity(neighborPos);

        if (neighborState.isOf(ModBlocks.PIPE)) {
            return true;
        }

        if (neighborBe instanceof PumpBlockEntity) {
            return checkPumpNeighbor(neighborState, direction);
        }

        if (!(neighborBe instanceof SpeciesTransportNode)) {
            return false;
        }

        if (!(world.getBlockEntity(pos) instanceof PipeBlockEntity pipe)) {
            return false;
        }

        SpeciesTransportNode node = (SpeciesTransportNode) neighborBe;

        // Pipe can connect if either side can insert/extract across that face.
        Direction toNeighbor = direction;
        Direction fromNeighbor = direction.getOpposite();

        return pipe.canExtract(toNeighbor)
                || pipe.canInsert(toNeighbor)
                || node.canExtract(fromNeighbor)
                || node.canInsert(fromNeighbor);
    }

    public static boolean client_connectsTo(BlockRenderView world, BlockPos pos, Direction direction) {
        BlockPos neighborPos = pos.offset(direction);
        BlockState neighborState = world.getBlockState(neighborPos);
        BlockEntity neighborBe = world.getBlockEntity(neighborPos);

        if (neighborState.isOf(ModBlocks.PIPE)) {
            return true;
        }

        if (neighborBe instanceof PumpBlockEntity) {
            return checkPumpNeighbor(neighborState, direction);
        }

        if (!(neighborBe instanceof SpeciesTransportNode)) {
            return false;
        }

        if (!(world.getBlockEntity(pos) instanceof PipeBlockEntity pipe)) {
            return false;
        }

        SpeciesTransportNode node = (SpeciesTransportNode) neighborBe;

        // Pipe can connect if either side can insert/extract across that face.
        Direction toNeighbor = direction;
        Direction fromNeighbor = direction.getOpposite();

        return pipe.canExtract(toNeighbor)
                || pipe.canInsert(toNeighbor)
                || node.canExtract(fromNeighbor)
                || node.canInsert(fromNeighbor);
    }


    @SuppressWarnings("static-access")
    public static boolean checkPumpNeighbor(BlockState state, Direction direction) {
        if (!(state.getBlock() instanceof PumpBlock pumpBlock)) {
            return false;
        }

        /// direction is facing towards the pump block
        /// but the FACING property will be the opposite way as it is facing towards the pipe
        return direction.getOpposite() == state.get(pumpBlock.FACING);
    }
}