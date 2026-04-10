package com.penguin.nuclide.pollution;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public final class PollutionManager {

    private PollutionManager() {}

    public static void addPollution(World world, BlockPos pos, int amount) {
        if (!(world instanceof ServerWorld serverWorld) || amount <= 0) return;

        PollutionState state = PollutionState.get(serverWorld);
        state.addPollution(new ChunkPos(pos), amount);
    }

    public static int getPollution(World world, BlockPos pos) {
        if (!(world instanceof ServerWorld serverWorld)) return 0;

        PollutionState state = PollutionState.get(serverWorld);
        return state.getPollution(new ChunkPos(pos));
    }

    public static void removePollution(World world, BlockPos pos, int amount) {
        if (!(world instanceof ServerWorld serverWorld) || amount <= 0) return;

        PollutionState state = PollutionState.get(serverWorld);
        state.removePollution(new ChunkPos(pos), amount);
    }
}