package com.penguin.nuclide.content.blockentities.electrolyser;

import com.penguin.nuclide.content.blockentities.base.AbstractReactionMachineBlockEntity;
import com.penguin.nuclide.content.registry.ModBlockEntities;
import com.penguin.nuclide.energy.EnergyNode;
import com.penguin.nuclide.energy.SimpleEnergyStorage;
import com.penguin.nuclide.reaction.ReactionDataLoader;
import com.penguin.nuclide.reaction.ReactionDefinition;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;

public class ElectrolyserBlockEntity extends AbstractReactionMachineBlockEntity implements EnergyNode {

    private final SimpleEnergyStorage energy = new SimpleEnergyStorage(200);

    public ElectrolyserBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ELECTROLYSER, pos, state);
    }

    @Override
    public int getBasePenaltyP() {
        return 5;
    }

    public static void tick(World world, BlockPos pos, BlockState state, ElectrolyserBlockEntity be) {
        if (world.isClient) return;
        be.tickServer();
    }

    @Override
    protected Iterable<ReactionDefinition> getCandidateReactions() {
        return ReactionDataLoader.REACTIONS.values();
    }
    
    @Override
    protected boolean supportsReaction(ReactionDefinition reaction) {
        return reaction.id().equals("nuclide:electrolysis");
    }

    @Override
    public boolean canInsertEnergy(Direction side) {
        if (energy.getStored() < energy.getCapacity()) {return true;}

        return false;
    }

    @Override
    public boolean canExtractEnergy(Direction side) {
        if (energy.getStored() > 0) {return true;}

        return false;
    }

    @Override
    public int insertEnergy(Direction side, int amount, boolean simulate) {
        if (canInsertEnergy(side)) {
            return energy.insert(amount, simulate);
        } else {
            return 0;
        }
    }

    @Override
    public int extractEnergy(Direction side, int amount, boolean simulate) {
        if (canExtractEnergy(side)) {
            return energy.extract(amount, simulate);
        } else {
            return 0;
        }
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
    protected void tickServer() {
        if (activeReaction == null || !canContinueReaction(activeReaction)) {
            activeReaction = findReaction();
            progress = 0;
        }

        if (activeReaction == null) {
            return;
        }

        final int energyCostPerTick = 20;

        if (energy.getStored() < energyCostPerTick) {
            return;
        }

        energy.extract(energyCostPerTick, false);
        super.tickServer();
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