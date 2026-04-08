package com.penguin.nuclide.content.blockentities.species_tank;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import com.penguin.nuclide.content.registry.ModBlockEntities;
import com.penguin.nuclide.species.SpeciesStack;
import com.penguin.nuclide.transport.SpeciesFilter;
import com.penguin.nuclide.transport.SpeciesTransportNode;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class SpeciesTankCasingBlockEntity extends BlockEntity implements SpeciesTransportNode {

    private static final String X = "cx";
    private static final String Y = "cy";
    private static final String Z = "cz";

    private BlockPos controllerPos;

    public SpeciesTankCasingBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SPECIES_TANK_CASING, pos, state);
    }

    public void setControllerPos(BlockPos pos) {
        controllerPos = pos != null ? pos.toImmutable() : null;
        markDirtyAndSync();
    }

    public void clearControllerPos() {
        controllerPos = null;
        markDirtyAndSync();
    }

    public BlockPos getControllerPos() {
        return controllerPos;
    }

    public @Nullable SpeciesTankControllerBlockEntity getController() {
        if (world == null || controllerPos == null) {
            return null;
        }

        if (world.getBlockEntity(controllerPos) instanceof SpeciesTankControllerBlockEntity be) {
            return be;
        }

        return null;
    }

    @Override
    public boolean canInsert(Direction side) {
        SpeciesTankControllerBlockEntity controller = getController();
        return controller != null && controller.canInsert(side);
    }

    @Override
    public List<SpeciesStack> getAvailableSpecies(Direction side) {
        SpeciesTankControllerBlockEntity controller = getController();
        if (controller == null) {
            return List.of();
        }
        return controller.getAvailableSpecies(side);
    }

    @Override
    public boolean canExtract(Direction side) {
        SpeciesTankControllerBlockEntity controller = getController();
        return controller != null && controller.canExtract(side);
    }

    @Override
    public int insertSpecies(Direction side, SpeciesStack stack, boolean simulate) {
        SpeciesTankControllerBlockEntity controller = getController();
        if (controller == null) {
            return 0;
        }

        return controller.insertSpecies(side, stack, simulate);
    }

    @Override
    public SpeciesStack extractSpecies(Direction side, SpeciesFilter filter, int maxAmount, boolean simulate) {
        SpeciesTankControllerBlockEntity controller = getController();
        if (controller == null) {
            return null;
        }

        return controller.extractSpecies(side, filter, maxAmount, simulate);
    }

    private void markDirtyAndSync() {
        markDirty();
        if (world instanceof ServerWorld sw) {
            sw.getChunkManager().markForUpdate(pos);
        }

        if (world != null) {
            BlockState state = getCachedState();
            world.updateListeners(pos, state, state, 3);
        }
    }
    
    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.writeNbt(nbt, lookup);
        if (controllerPos != null) {
            nbt.putInt(X, controllerPos.getX());
            nbt.putInt(Y, controllerPos.getY());
            nbt.putInt(Z, controllerPos.getZ());
        }
    }

    @Override
    protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        super.readNbt(nbt, lookup);
        if (nbt.contains(X)) {
            controllerPos = new BlockPos(nbt.getInt(X), nbt.getInt(Y), nbt.getInt(Z));
        } else {
            controllerPos = null;
        }
    }

    @Override
    public BlockEntityUpdateS2CPacket toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup lookup) {
        return createNbt(lookup);
    }
}