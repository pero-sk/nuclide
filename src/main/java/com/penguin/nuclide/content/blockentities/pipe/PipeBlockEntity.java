package com.penguin.nuclide.content.blockentities.pipe;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.penguin.nuclide.Nuclide;
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
    private static final int TRANSFER_RATE = Nuclide.DEFAULT_TRANSFER_RATE;

    private static final String ROUND_ROBIN_INDEX_KEY = "round_robin_index";
    private static final String LAST_INSERTED_FROM_KEY = "last_inserted_from";
    private static final String FILTER_SPECIES_ID_KEY = "filter_species_id";

    private final SpeciesMixture buffer = new SpeciesMixture(BUFFER_CAPACITY);

    private int roundRobinIndex = 0;
    private @Nullable Direction lastInsertedFrom = null;
    private @Nullable String filterSpeciesId = null;

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

        boolean movedAnything = false;
        List<SpeciesStack> bufferedStacks = getBufferedStacks();

        for (SpeciesStack stack : bufferedStacks) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }

            movedAnything |= pushSpecies(world, pos, stack.speciesId());
        }

        if (movedAnything) {
            markDirty();
        }
    }

    private boolean pushSpecies(World world, BlockPos pos, String speciesId) {
        int available = buffer.amountOf(speciesId);
        if (available <= 0) {
            return false;
        }

        int toMove = Math.min(available, TRANSFER_RATE);
        List<Direction> outputs = findValidOutputs(world, pos, speciesId);

        if (outputs.isEmpty()) {
            return false;
        }

        if (outputs.size() > 1 && lastInsertedFrom != null) {
            outputs.remove(lastInsertedFrom);
        }

        if (outputs.isEmpty()) {
            return false;
        }

        Direction output = pickRoundRobin(outputs);
        return tryPushSpeciesTo(world, pos, output, speciesId, toMove);
    }

    private List<Direction> findValidOutputs(World world, BlockPos pos, String speciesId) {
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

            // If neighbor is another pipe, honor its whole-pipe input filter.
            if (be instanceof PipeBlockEntity pipe) {
                if (!pipe.acceptsSpecies(speciesId)) {
                    continue;
                }
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

    private boolean tryPushSpeciesTo(World world, BlockPos pos, Direction dir, String speciesId, int maxAmount) {
        BlockPos neighborPos = pos.offset(dir);
        SpeciesTransportNode target = findTransportNode(world, neighborPos);
        if (target == null) {
            return false;
        }

        Direction targetSide = dir.getOpposite();
        if (!target.canInsert(targetSide)) {
            return false;
        }

        SpeciesStack simulated = buffer.extract(speciesId, maxAmount, true);
        if (simulated == null || simulated.isEmpty()) {
            return false;
        }

        int accepted = target.insertSpecies(targetSide, simulated, true);
        if (accepted <= 0) {
            return false;
        }

        SpeciesStack extracted = buffer.extract(speciesId, accepted, false);
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

    private List<SpeciesStack> getBufferedStacks() {
        List<SpeciesStack> stacks = new ArrayList<>();
        for (String speciesId : buffer.speciesIds()) {
            int amount = buffer.amountOf(speciesId);
            if (amount > 0) {
                stacks.add(new SpeciesStack(speciesId, amount));
            }
        }
        return stacks;
    }

    private @Nullable SpeciesTransportNode findTransportNode(World world, BlockPos pos) {
        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof SpeciesTransportNode node) {
            return node;
        }
        return null;
    }

    public boolean acceptsSpecies(String speciesId) {
        return filterSpeciesId == null || filterSpeciesId.equals(speciesId);
    }

    @Override
    public List<SpeciesStack> getAvailableSpecies(Direction side) {
        return getBufferedStacks();
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
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        if (!acceptsSpecies(stack.speciesId())) {
            return 0;
        }

        int inserted = buffer.insert(stack, simulate);

        if (!simulate && inserted > 0) {
            lastInsertedFrom = side;
            markDirty();
        }

        return inserted;
    }

    @Override
    public SpeciesStack extractSpecies(Direction side, SpeciesFilter filter, int maxAmount, boolean simulate) {
        SpeciesFilter actualFilter = filter != null ? filter : SpeciesFilter.any();

        for (SpeciesStack stack : getBufferedStacks()) {
            if (!actualFilter.test(stack)) {
                continue;
            }

            return buffer.extract(stack.speciesId(), maxAmount, simulate);
        }

        return null;
    }

    public int getBufferedAmount() {
        return buffer.totalAmount();
    }

    public @Nullable String getFilterSpeciesId() {
        return filterSpeciesId;
    }

    public void setFilterSpeciesId(@Nullable String speciesId) {
        this.filterSpeciesId = speciesId;
        markDirty();
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);

        nbt.putInt(ROUND_ROBIN_INDEX_KEY, roundRobinIndex);

        if (lastInsertedFrom != null) {
            nbt.putString(LAST_INSERTED_FROM_KEY, lastInsertedFrom.getName());
        }

        if (filterSpeciesId != null) {
            nbt.putString(FILTER_SPECIES_ID_KEY, filterSpeciesId);
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

        if (nbt.contains(FILTER_SPECIES_ID_KEY)) {
            filterSpeciesId = nbt.getString(FILTER_SPECIES_ID_KEY);
        } else {
            filterSpeciesId = null;
        }
    }
}