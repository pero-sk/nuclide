package com.penguin.nuclide.content.blockentities;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.penguin.nuclide.content.registry.ModBlockEntities;
import com.penguin.nuclide.species.SpeciesStack;
import com.penguin.nuclide.transport.SpeciesFilter;
import com.penguin.nuclide.transport.SpeciesMixture;
import com.penguin.nuclide.transport.SpeciesTransportNode;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class PipeBlockEntity extends BlockEntity implements SpeciesTransportNode {
    private static final int BUFFER_CAPACITY = 200;
    private static final int TRANSFER_RATE = 50;

    private static final String ROUND_ROBIN_INDEX_KEY = "round_robin_index";
    private static final String LAST_INSERTED_FROM_KEY = "last_inserted_from";

    private static final SpeciesMixture buffer = new SpeciesMixture(BUFFER_CAPACITY);

    private int roundRobinIndex = 0;
    private @Nullable Direction lastInsertedFrom = null;

    public PipeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PIPE, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, PipeBlockEntity pipe) {
        if (world.isClient) {
            return;
        }

        pipe.pushContents(world, pos);
    }

    private void pushContents(World world, BlockPos pos) {
        if (buffer.isEmpty()) {
            return;
        }

        List<Direction> outputs = findValidOutputs(world, pos);

        if (outputs.isEmpty()) {
            return;
        }

        if (outputs.size() > 1 && lastInsertedFrom != null) {
            outputs.remove(lastInsertedFrom);
        }

        if (outputs.isEmpty()) {
            return;
        }

        Direction output = pickRoundRobin(outputs);
        boolean moved = tryPushTo(world, pos, output);

        if (moved) {
            markDirty();
        }
    }

    private List<Direction> findValidOutputs(World world, BlockPos pos) {
        List<Direction> nonPipeOutputs = new ArrayList<>();
        List<Direction> pipeOutputs = new ArrayList<>();

        for (Direction dir : Direction.values()) {
            BlockPos neighborPos = pos.offset(dir);
            BlockEntity be = world.getBlockEntity(neighborPos);

            if (!(be instanceof SpeciesTransportNode node)) {
                continue;
            }

            Direction targetSide = dir.getOpposite();
            if (!node.canInsert(targetSide)) {
                continue;
            }

            if (be instanceof PipeBlockEntity) {
                pipeOutputs.add(dir);
            } else {
                nonPipeOutputs.add(dir);
            }
        }

        nonPipeOutputs.addAll(pipeOutputs);
        return nonPipeOutputs;
    }

    private Direction pickRoundRobin(List<Direction> outputs) {
        if (outputs.isEmpty()) {
            throw new IllegalArgumentException("outputs cannot be empty");
        }

        int index = Math.floorMod(roundRobinIndex, outputs.size());
        Direction chosen = outputs.get(index);
        roundRobinIndex = (index + 1) % outputs.size();
        return chosen;
    }

    private boolean tryPushTo(World world, BlockPos pos, Direction dir) {
        BlockPos neighborPos = pos.offset(dir);
        SpeciesTransportNode target = findTransportNode(world, neighborPos);
        if (target == null) {
            return false;
        }

        Direction targetSide = dir.getOpposite();
        if (!target.canInsert(targetSide)) {
            return false;
        }

        SpeciesStack simulated = extractSpecies(dir, SpeciesFilter.any(), TRANSFER_RATE, true);
        if (simulated == null || simulated.isEmpty()) {
            return false;
        }

        int accepted = target.insertSpecies(targetSide, simulated, true);
        if (accepted <= 0) {
            return false;
        }

        SpeciesStack extracted = extractSpecies(dir, SpeciesFilter.any(), accepted, false);
        if (extracted == null || extracted.isEmpty()) {
            return false;
        }

        int inserted = target.insertSpecies(targetSide, extracted, false);
        if (inserted != extracted.count()) {
            throw new IllegalStateException(
                    "Pipe desync: extracted " + extracted.count()
                            + " of " + extracted.speciesId()
                            + " but inserted " + inserted
            );
        }

        BlockEntity targetBe = world.getBlockEntity(neighborPos);
        if (targetBe != null) {
            targetBe.markDirty();
        }

        return true;
    }

    private @Nullable SpeciesTransportNode findTransportNode(World world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof SpeciesTransportNode node) {
            return node;
        }
        return null;
    }

    @Override
    public boolean canInsert(Direction side) {
        return side != null;
    }

    @Override
    public boolean canExtract(Direction side) {
        return side != null && !buffer.isEmpty();
    }

    @Override
    public int insertSpecies(Direction side, SpeciesStack stack, boolean simulate) {
        int inserted = buffer.insert(stack, simulate);

        if (!simulate && inserted > 0) {
            lastInsertedFrom = side;
            markDirty();
        }

        return inserted;
    }

    @Override
    public SpeciesStack extractSpecies(Direction side, SpeciesFilter filter, int maxAmount, boolean simulate) {
        return buffer.extractAny(filter, maxAmount, simulate);
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);

        nbt.putInt(ROUND_ROBIN_INDEX_KEY, roundRobinIndex);

        if (lastInsertedFrom != null) {
            nbt.putString(LAST_INSERTED_FROM_KEY, lastInsertedFrom.getName());
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);

        roundRobinIndex = nbt.getInt(ROUND_ROBIN_INDEX_KEY);

        if (nbt.contains(LAST_INSERTED_FROM_KEY)) {
            lastInsertedFrom = Direction.byName(nbt.getString(LAST_INSERTED_FROM_KEY));
        } else {
            lastInsertedFrom = null;
        }
    }

    public int getBufferedAmount() {
        return buffer.totalAmount();
    }
}