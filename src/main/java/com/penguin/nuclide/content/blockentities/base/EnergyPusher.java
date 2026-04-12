package com.penguin.nuclide.content.blockentities.base;

import java.util.Set;

import com.penguin.nuclide.energy.EnergyNode;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public interface EnergyPusher extends EnergyNode {

    int getMaxTransferPerTick();

    Set<Direction> getRecentlyReceivedEnergySides();

    default void clearRecentlyReceivedEnergySides() {
        getRecentlyReceivedEnergySides().clear();
    }

    default void markReceivedEnergyFrom(Direction side) {
        if (side != null) {
            getRecentlyReceivedEnergySides().add(side);
        }
    }

    default boolean canPushEnergyTo(Direction side) {
        return canExtractEnergy(side) && !getRecentlyReceivedEnergySides().contains(side);
    }

    default void pushEnergyToNeighbors() {
        if (!(this instanceof BlockEntity self)) {
            throw new IllegalStateException("EnergyPusher must be implemented by a BlockEntity");
        }

        World world = self.getWorld();
        BlockPos pos = self.getPos();

        if (world == null || getStoredEnergy() <= 0) {
            clearRecentlyReceivedEnergySides();
            return;
        }

        boolean changed = false;

        for (Direction dir : Direction.values()) {
            if (getStoredEnergy() <= 0) {
                break;
            }

            if (!canPushEnergyTo(dir)) {
                continue;
            }

            BlockEntity neighborBe = world.getBlockEntity(pos.offset(dir));
            if (!(neighborBe instanceof EnergyNode neighbor)) {
                continue;
            }

            Direction neighborSide = dir.getOpposite();

            if (!neighbor.canInsertEnergy(neighborSide)) {
                continue;
            }

            int available = Math.min(getStoredEnergy(), getMaxTransferPerTick());
            if (available <= 0) {
                break;
            }

            int accepted = neighbor.insertEnergy(neighborSide, available, true);
            if (accepted <= 0) {
                continue;
            }

            int extracted = extractEnergy(dir, accepted, false);
            if (extracted <= 0) {
                continue;
            }

            int inserted = neighbor.insertEnergy(neighborSide, extracted, false);
            if (inserted > 0) {
                changed = true;
            }
        }

        if (changed) {
            self.markDirty();
            world.updateListeners(pos, self.getCachedState(), self.getCachedState(), 3);
        }

        clearRecentlyReceivedEnergySides();
    }
}