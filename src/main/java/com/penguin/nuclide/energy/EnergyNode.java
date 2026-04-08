package com.penguin.nuclide.energy;

import net.minecraft.util.math.Direction;

public interface EnergyNode {
    boolean canInsertEnergy(Direction side);

    boolean canExtractEnergy(Direction side);

    int insertEnergy(Direction side, int amount, boolean simulate);

    int extractEnergy(Direction side, int amount, boolean simulate);

    int getStoredEnergy();

    int getEnergyCapacity();
}