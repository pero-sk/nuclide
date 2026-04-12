package com.penguin.nuclide.content.blockentities.cable;

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

public class CableBlockEntity extends BlockEntity implements EnergyPusher {

    private static final int DEFAULT_CAPACITY = 200;
    private static final int MAX_TRANSFER = 40;

    private final SimpleEnergyStorage energy = new SimpleEnergyStorage(DEFAULT_CAPACITY);
    private final Set<Direction> recentlyReceivedEnergySides = EnumSet.noneOf(Direction.class);

    public CableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CABLE, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, CableBlockEntity cable) {
        if (world.isClient) {
            return;
        }

        cable.pushEnergyToNeighbors();
    }

    @Override
    public boolean canInsertEnergy(Direction side) {
        return energy.getStored() < energy.getCapacity();
    }

    @Override
    public boolean canExtractEnergy(Direction side) {
        return energy.getStored() > 0;
    }

    @Override
    public int insertEnergy(Direction side, int amount, boolean simulate) {
        if (!canInsertEnergy(side)) {
            return 0;
        }

        int inserted = energy.insert(amount, simulate);

        if (!simulate && inserted > 0) {
            markReceivedEnergyFrom(side);
        }

        return inserted;
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
    public int getMaxTransferPerTick() {
        return MAX_TRANSFER;
    }

    @Override
    public Set<Direction> getRecentlyReceivedEnergySides() {
        return recentlyReceivedEnergySides;
    }
}