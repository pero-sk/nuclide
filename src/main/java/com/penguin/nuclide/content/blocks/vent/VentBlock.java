package com.penguin.nuclide.content.blocks.vent;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.penguin.nuclide.content.blockentities.vent.VentBlockEntity;
import com.penguin.nuclide.content.registry.ModBlockEntities;
import com.penguin.nuclide.content.registry.ModItems;
import com.penguin.nuclide.nbt.NuclideDataComponents;
import com.penguin.nuclide.species.SpeciesContainer;
import com.penguin.nuclide.species.SpeciesStack;

import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class VentBlock extends BlockWithEntity {

    public static final MapCodec<VentBlock> CODEC = createCodec(VentBlock::new);

    public VentBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new VentBlockEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ModBlockEntities.VENT, VentBlockEntity::tick);
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
        if (world.isClient) {
            return ItemActionResult.SUCCESS;
        }

        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof VentBlockEntity vent)) {
            return ItemActionResult.FAIL;
        }

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
            int inserted = vent.insertSpecies(null, speciesStack, false);
            int leftover = speciesStack.count() - inserted;

            if (leftover > 0) {
                remaining.add(speciesStack.key(), leftover);
            }
        }

        ItemStack newStack = new ItemStack(ModItems.CANISTER_ITEM);

        if (!remaining.isEmpty()) {
            newStack.set(NuclideDataComponents.SPECIES_CONTAINER, remaining);
            player.sendMessage(Text.literal("Inserted ventable contents; some remained"), false);
        } else {
            player.sendMessage(Text.literal("Inserted contents into vent"), false);
        }

        player.setStackInHand(hand, ItemStack.EMPTY);
        player.dropItem(newStack, false);

        return ItemActionResult.SUCCESS;
    }
}