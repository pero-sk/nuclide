package com.penguin.nuclide.content.blockentities.hand_crank;

import java.util.EnumSet;
import java.util.Set;

import com.penguin.nuclide.content.blockentities.base.EnergyPusher;
import com.penguin.nuclide.content.registry.ModBlockEntities;
import com.penguin.nuclide.energy.SimpleEnergyStorage;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class HandCrankBlockEntity extends BlockEntity implements EnergyPusher {

    private static final int ENERGY_CAPACITY = 200;
    private static final int MAX_TRANSFER_PER_TICK = 20;

    private final SimpleEnergyStorage energy = new SimpleEnergyStorage(ENERGY_CAPACITY);
    private final Set<Direction> recentlyReceivedEnergySides = EnumSet.noneOf(Direction.class);

    public HandCrankBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HAND_CRANK, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, HandCrankBlockEntity be) {
        if (world.isClient) {
            return;
        }

        be.pushEnergyToNeighbors();
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
    public boolean canInsertEnergy(Direction side) {
        return false;
    }

    @Override
    public boolean canExtractEnergy(Direction side) {
        return energy.getStored() > 0;
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

    public void crank() {
        int inserted = energy.insert(20, false);

        if (inserted > 0) {
            markDirty();

            if (world != null) {
                world.updateListeners(pos, getCachedState(), getCachedState(), 3);
            }
        }
    }
}