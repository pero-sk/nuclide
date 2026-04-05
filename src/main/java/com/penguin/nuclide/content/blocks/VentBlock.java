package com.penguin.nuclide.content.blocks;

import com.penguin.nuclide.data.NuclideDataLoader;
import com.penguin.nuclide.data.SpeciesDefinition;
import com.penguin.nuclide.nbt.NuclideDataComponents;
import com.penguin.nuclide.species.SpeciesContainer;
import com.penguin.nuclide.species.SpeciesStack;
import com.penguin.nuclide.Nuclide;
import com.penguin.nuclide.content.registry.ModItems;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VentBlock extends Block {

    public VentBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected ItemActionResult onUseWithItem(
            ItemStack stack,
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            Hand hand,
            BlockHitResult hit
    ) {

        if (world.isClient) return ItemActionResult.SUCCESS;

        ItemStack newStack = new ItemStack(ModItems.CANISTER_ITEM);

        SpeciesContainer container = stack.getOrDefault(
                NuclideDataComponents.SPECIES_CONTAINER,
                new SpeciesContainer()
        );

        if (container.isEmpty()) {
            player.sendMessage(Text.literal("Canister is empty"), false);
            return ItemActionResult.SUCCESS;
        }

        SpeciesContainer remaining = new SpeciesContainer();

        for (SpeciesStack speciesStack : container.stacks()) {
            SpeciesDefinition species =
                    NuclideDataLoader.SPECIES.getById(speciesStack.speciesId());

            if (species == null) {
                remaining.add(speciesStack);
                continue;
            }

            var rep = species.representation();

            var phaseRep = switch (species.state()) {
                case GAS -> rep.gas();
                case LIQUID -> rep.liquid();
                case SOLID -> rep.solid();
                case PLASMA -> rep.gas(); // default to gas for plasma for now
                default -> throw new IllegalArgumentException("Unexpected value: " + species.state());
            };

            if (phaseRep == null) {
                remaining.add(speciesStack);
                continue;
            }

            String blockIdString = phaseRep.block();
            if (blockIdString == null) {
                remaining.add(speciesStack);
                continue;
            }

            Identifier blockId = Identifier.of(blockIdString);

            var block = Registries.BLOCK.get(blockId);

            if (block == null) {
                remaining.add(speciesStack);
                continue;
            }

            // spawn gas
            releaseGas((ServerWorld) world, pos, block, speciesStack.count());
        }

        player.sendMessage(Text.literal("Released contents"), false);

        if (!remaining.isEmpty()) {
            newStack.set(NuclideDataComponents.SPECIES_CONTAINER, remaining);
        }

        player.setStackInHand(hand, ItemStack.EMPTY);
        player.dropItem(newStack, false);

        return ItemActionResult.SUCCESS;
    }

    private void releaseGas(ServerWorld world, BlockPos pos, Block block, int amount) {
        int fullBlocks = amount / Nuclide.MMOL_PER_BLOCK;

        if (amount < Nuclide.MMOL_PER_BLOCK) {
            return; // discard
        }

        // place full blocks
        for (int i = 0; i < fullBlocks && i < 20; i++) {
            placeGas(world, pos, block, 15);
        }
    }
    
    private void placeGas(ServerWorld world, BlockPos pos, Block block, int level) {
        BlockPos target = pos.up().add(
                world.random.nextInt(2),
                world.random.nextInt(1),
                world.random.nextInt(2)
        );

        if (!world.getBlockState(target).isAir()) return;

        var state = block.getDefaultState();

        if (state.contains(GasBlock.LEVEL)) {
            state = state.with(GasBlock.LEVEL, level);
        }

        world.setBlockState(target, state);
    }
}
