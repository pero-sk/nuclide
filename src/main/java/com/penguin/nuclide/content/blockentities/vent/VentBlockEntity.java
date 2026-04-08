package com.penguin.nuclide.content.blockentities.vent;

import java.util.List;

import com.penguin.nuclide.Nuclide;
import com.penguin.nuclide.content.blocks.gas.GasBlock;
import com.penguin.nuclide.content.registry.ModBlockEntities;
import com.penguin.nuclide.data.NuclideDataLoader;
import com.penguin.nuclide.data.SpeciesDefinition;
import com.penguin.nuclide.species.SpeciesContainer;
import com.penguin.nuclide.species.SpeciesStack;
import com.penguin.nuclide.transport.SpeciesFilter;
import com.penguin.nuclide.transport.SpeciesTransportNode;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class VentBlockEntity extends BlockEntity implements SpeciesTransportNode {

    private static final int MAX_INTERNAL = Nuclide.MMOL_PER_BLOCK * 8;
    private final SpeciesContainer container = new SpeciesContainer();

    public VentBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VENT, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, VentBlockEntity vent) {
        if (world.isClient) {
            return;
        }

        vent.tickServer((ServerWorld) world);
    }

    private void tickServer(ServerWorld world) {
        if (container.isEmpty()) {
            return;
        }

        boolean changed = false;

        for (SpeciesStack stack : container.stacks()) {
            if (stack == null || stack.isEmpty()) {
                continue;
            }

            SpeciesDefinition species = NuclideDataLoader.SPECIES.getById(stack.speciesId());
            if (species == null || species.representation() == null || species.representation().gas() == null) {
                continue;
            }

            String blockIdString = species.representation().gas().block();
            if (blockIdString == null) {
                continue;
            }

            Block block = Registries.BLOCK.get(Identifier.of(blockIdString));
            if (block == null) {
                continue;
            }

            if (stack.count() < Nuclide.MMOL_PER_BLOCK) {
                continue;
            }

            boolean placed = placeGas(world, pos, block, 15);
            if (!placed) {
                continue;
            }

            container.remove(stack.key(), Nuclide.MMOL_PER_BLOCK);
            changed = true;
        }

        if (changed) {
            markDirty();
            world.updateListeners(pos, getCachedState(), getCachedState(), 3);
        }
    }

    private boolean placeGas(ServerWorld world, BlockPos pos, Block block, int level) {
        for (int i = 0; i < 8; i++) {
            BlockPos target = pos.up().add(
                    world.random.nextInt(3) - 1,
                    world.random.nextInt(2),
                    world.random.nextInt(3) - 1
            );

            if (!world.getBlockState(target).isAir()) {
                continue;
            }

            BlockState state = block.getDefaultState();
            if (state.contains(GasBlock.LEVEL)) {
                state = state.with(GasBlock.LEVEL, level);
            }

            world.setBlockState(target, state);
            return true;
        }

        return false;
    }

    private int totalStored() {
        int total = 0;
        for (SpeciesStack stack : container.stacks()) {
            total += stack.count();
        }
        return total;
    }

    @Override
    public boolean canInsert(Direction side) {
        return totalStored() < MAX_INTERNAL;
    }

    @Override
    public boolean canExtract(Direction side) {
        return false;
    }

    @Override
    public int insertSpecies(Direction side, SpeciesStack stack, boolean simulate) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        SpeciesDefinition species = NuclideDataLoader.SPECIES.getById(stack.speciesId());
        if (species == null || species.representation() == null || species.representation().gas() == null) {
            return 0;
        }

        String blockId = species.representation().gas().block();
        if (blockId == null) {
            return 0;
        }

        int space = MAX_INTERNAL - totalStored();
        if (space <= 0) {
            return 0;
        }

        int inserted = Math.min(space, stack.count());

        if (!simulate && inserted > 0) {
            container.add(stack.key(), inserted);
            markDirty();

            if (world != null) {
                world.updateListeners(pos, getCachedState(), getCachedState(), 3);
            }
        }

        return inserted;
    }

    @Override
    public SpeciesStack extractSpecies(Direction side, SpeciesFilter filter, int maxAmount, boolean simulate) {
        return null;
    }

    @Override
    public List<SpeciesStack> getAvailableSpecies(Direction side) {
        return List.of();
    }

    public SpeciesContainer getContainer() {
        return container;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.writeNbt(nbt, registryLookup);
        nbt.put("Container", container.toNbtList());
    }

    @Override
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        super.readNbt(nbt, registryLookup);

        if (nbt.contains("Container", NbtElement.LIST_TYPE)) {
            container.fromNbtList(nbt.getList("Container", NbtElement.COMPOUND_TYPE));
        } else {
            container.clear();
        }
    }
}