package com.penguin.nuclide.content.blockentities.creative.energiser;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import com.penguin.nuclide.Nuclide;
import com.penguin.nuclide.content.blockentities.base.EnergyPusher;
import com.penguin.nuclide.content.registry.ModBlockEntities;
import com.penguin.nuclide.misc.IHaveHoverInformation;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class CreativeEnergiserBlockEntity extends BlockEntity implements EnergyPusher, IHaveHoverInformation {

    private final Set<Direction> recentlyReceivedEnergySides = EnumSet.noneOf(Direction.class);

    public CreativeEnergiserBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CREATIVE_ENERGISER, pos, state);
    }

    @Override
    public boolean canInsertEnergy(Direction side) {
        return false;
    }

    @Override
    public boolean canExtractEnergy(Direction side) {
        return true;
    }

    @Override
    public int insertEnergy(Direction side, int amount, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(Direction side, int amount, boolean simulate) {
        return Math.min(amount, getMaxTransferPerTick());
    }

    @Override
    public int getStoredEnergy() {
        return getEnergyCapacity();
    }

    @Override
    public int getEnergyCapacity() {
        return 60000;
    }

    @Override
    public int getMaxTransferPerTick() {
        return Nuclide.DEFAULT_TRANSFER_RATE;
    }

    @Override
    public Set<Direction> getRecentlyReceivedEnergySides() {
        return recentlyReceivedEnergySides;
    }

    @Override
    public boolean addHoverInformation(World world, BlockHitResult hit, PlayerEntity player, List<Text> tooltip) {
        tooltip.add(Text.literal("Creative Energiser"));

        tooltip.add(Text.literal("Energy: "+getStoredEnergy()+" / "+getEnergyCapacity()));
        
        return true;
    }

    public static void tick(World world, BlockPos pos, BlockState state, CreativeEnergiserBlockEntity energiser) {
        if (world.isClient) {
            return;
        }

        energiser.pushEnergyToNeighbors();
    }
}