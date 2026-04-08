package com.penguin.nuclide.content.blocks.pipe;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.penguin.nuclide.content.blockentities.pipe.PipeBlockEntity;
import com.penguin.nuclide.content.items.SpeciesFilterItem;
import com.penguin.nuclide.content.registry.ModBlockEntities;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PipeBlock extends BlockWithEntity {

    public static final MapCodec<PipeBlock> CODEC = createCodec(PipeBlock::new);

    public PipeBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PipeBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            World world,
            BlockState state,
            BlockEntityType<T> type
    ) {
        return validateTicker(
                type,
                ModBlockEntities.PIPE,
                PipeBlockEntity::tick
        );
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
        if (hand != Hand.MAIN_HAND) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        if (world.isClient) {
            return ItemActionResult.SUCCESS;
        }

        if (!(stack.getItem() instanceof SpeciesFilterItem)) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof PipeBlockEntity pipe)) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        String selectedSpecies = SpeciesFilterItem.getSelectedSpecies(stack);

        if (selectedSpecies == null || selectedSpecies.isBlank()) {
            pipe.setFilterSpeciesId(null);
            player.sendMessage(Text.literal("Cleared pipe filter"), false);
            return ItemActionResult.SUCCESS;
        }

        pipe.setFilterSpeciesId(selectedSpecies);
        player.sendMessage(
                Text.literal("Set pipe filter to " + selectedSpecies),
                false
        );
        return ItemActionResult.SUCCESS;
    }

    @Override
    protected ActionResult onUse(
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            BlockHitResult hit
    ) {
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof PipeBlockEntity pipe)) {
            return ActionResult.PASS;
        }

        String filter = pipe.getFilterSpeciesId();

        if (filter == null) {
            player.sendMessage(Text.literal("Pipe filter: none"), false);
        } else {
            player.sendMessage(Text.literal("Pipe filter: " + filter), false);
        }

        return ActionResult.SUCCESS;
    }
}