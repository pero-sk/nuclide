package com.penguin.nuclide.content.blockentities.hydrogen_furnace;

import java.util.EnumSet;
import java.util.Set;

import com.penguin.nuclide.content.blockentities.base.AbstractReactionMachineBlockEntity;
import com.penguin.nuclide.content.blockentities.base.EnergyPusher;
import com.penguin.nuclide.content.registry.ModBlockEntities;
import com.penguin.nuclide.energy.SimpleEnergyStorage;
import com.penguin.nuclide.reaction.ReactionDataLoader;
import com.penguin.nuclide.reaction.ReactionDefinition;
import com.penguin.nuclide.species.SpeciesStack;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class HydrogenFurnaceBlockEntity extends AbstractReactionMachineBlockEntity implements EnergyPusher {

    private static final int ENERGY_CAPACITY = 600;
    private static final int MAX_TRANSFER_PER_TICK = ENERGY_CAPACITY / 10;
    private static final int BURN_ENERGY_AMOUNT = 200;

    private final SimpleEnergyStorage energy = new SimpleEnergyStorage(ENERGY_CAPACITY);
    private final Set<Direction> recentlyReceivedEnergySides = EnumSet.noneOf(Direction.class);

    public HydrogenFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HYDROGEN_FURNACE, pos, state);
    }

    @Override
    public int getBasePenaltyP() {
        return 8;
    }

    @Override
    public boolean canInsertEnergy(Direction side) {
        return false;
    }

    @Override
    public boolean canExtractEnergy(Direction side) {
        return energy.getStored() > 0;
    }

    @Override
    public int insertSpecies(Direction side, SpeciesStack stack, boolean simulate) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        String id = stack.speciesId();
        if (!"molecules:hydrogen_gas".equals(id) && !"molecules:oxygen_gas".equals(id)) {
            return 0;
        }

        return super.insertSpecies(side, stack, simulate);
    }

    @Override
    public int insertEnergy(Direction side, int amount, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(Direction side, int amount, boolean simulate) {
        return canExtractEnergy(side) ? energy.extract(amount, simulate) : 0;
    }

    @Override
    public int getStoredEnergy() {
        return energy.getStored();
    }

    @Override
    public int getEnergyCapacity() {
        return energy.getCapacity();
    }

    @Override
    protected Iterable<ReactionDefinition> getCandidateReactions() {
        return ReactionDataLoader.REACTIONS.values();
    }

    @Override
    protected void onReactionCompleted(ReactionDefinition reaction) {
        if (!"nuclide:hydrogen_burn".equals(reaction.id())) {
            return;
        }

        int inserted = energy.insert(BURN_ENERGY_AMOUNT, false);
        if (inserted > 0) {
            markDirty();

            if (world != null) {
                world.updateListeners(pos, getCachedState(), getCachedState(), 3);
            }
        }
    }

    @Override
    protected boolean supportsReaction(ReactionDefinition reaction) {
        return "nuclide:hydrogen_burn".equals(reaction.id());
    }

    public static void tick(World world, BlockPos pos, BlockState state, HydrogenFurnaceBlockEntity furnace) {
        if (world.isClient) {
            return;
        }

        furnace.tickServer();
        furnace.pushEnergyToNeighbors();
    }

    @Override
    public int getMaxTransferPerTick() {
        return MAX_TRANSFER_PER_TICK;
    }

    @Override
    public Set<Direction> getRecentlyReceivedEnergySides() {
        return recentlyReceivedEnergySides;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.putInt("Energy", energy.getStored());
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);
        energy.setStored(nbt.getInt("Energy"));
    }
}