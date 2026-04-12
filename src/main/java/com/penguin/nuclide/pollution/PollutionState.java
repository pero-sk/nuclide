package com.penguin.nuclide.pollution;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;

public class PollutionState extends PersistentState {

    private static final String DATA_KEY = "pollution";
    private final Map<Long, Integer> pollutionByChunk = new HashMap<>();

    public PollutionState() {}

    public static PollutionState createFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        PollutionState state = new PollutionState();

        NbtList list = nbt.getList(DATA_KEY, NbtCompound.COMPOUND_TYPE);
        for (int i = 0; i < list.size(); i++) {
            NbtCompound entry = list.getCompound(i);
            int chunkX = entry.getInt("x");
            int chunkZ = entry.getInt("z");
            int amount = entry.getInt("amount");

            if (amount > 0) {
                state.pollutionByChunk.put(ChunkPos.toLong(chunkX, chunkZ), amount);
            }
        }

        return state;
    }

    public static PollutionState get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
            new PersistentState.Type<>(
                PollutionState::new,
                PollutionState::createFromNbt,
                null
            ),
            "nuclide_pollution"
        );
    }

    public int getPollution(ChunkPos chunkPos) {
        return pollutionByChunk.getOrDefault(chunkPos.toLong(), 0);
    }

    public void addPollution(ChunkPos chunkPos, int amount) {
        if (amount <= 0) return;

        long key = chunkPos.toLong();
        pollutionByChunk.merge(key, amount, Integer::sum);
        markDirty();
    }

    public void removePollution(ChunkPos chunkPos, int amount) {
        if (amount <= 0) return;

        long key = chunkPos.toLong();
        int current = pollutionByChunk.getOrDefault(key, 0);
        int updated = Math.max(0, current - amount);

        if (updated == 0) {
            pollutionByChunk.remove(key);
        } else {
            pollutionByChunk.put(key, updated);
        }

        markDirty();
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList list = new NbtList();

        for (Map.Entry<Long, Integer> entry : pollutionByChunk.entrySet()) {
            if (entry.getValue() <= 0) continue;

            ChunkPos chunkPos = new ChunkPos(entry.getKey());

            NbtCompound tag = new NbtCompound();
            tag.putInt("x", chunkPos.x);
            tag.putInt("z", chunkPos.z);
            tag.putInt("amount", entry.getValue());

            list.add(tag);
        }

        nbt.put(DATA_KEY, list);
        return nbt;
    }
}