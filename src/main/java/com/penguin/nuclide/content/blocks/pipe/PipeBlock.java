package com.penguin.nuclide.content.blocks.pipe;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.penguin.nuclide.content.blockentities.pipe.PipeBlockEntity;
import com.penguin.nuclide.content.items.SpeciesFilterItem;
import com.penguin.nuclide.content.registry.ModBlockEntities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
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
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
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



    /// SHAPE
    
    private static final VoxelShape CORE_SHAPE = Block.createCuboidShape(
            5.0, 5.0, 5.0,
            11.0, 11.0, 11.0
    );

    private static final VoxelShape NORTH_ARM_SHAPE = Block.createCuboidShape(
            5.5, 6.0, 0.0,
            10.5, 10.0, 5.0
    );

    private static final VoxelShape SOUTH_ARM_SHAPE = Block.createCuboidShape(
            5.5, 6.0, 11.0,
            10.5, 10.0, 16.0
    );

    private static final VoxelShape WEST_ARM_SHAPE = Block.createCuboidShape(
            0.0, 6.0, 5.5,
            5.0, 10.0, 10.5
    );

    private static final VoxelShape EAST_ARM_SHAPE = Block.createCuboidShape(
            11.0, 6.0, 5.5,
            16.0, 10.0, 10.5
    );

    private static final VoxelShape DOWN_ARM_SHAPE = Block.createCuboidShape(
            5.5, 0.0, 5.5,
            10.5, 6.0, 10.5
    );

    private static final VoxelShape UP_ARM_SHAPE = Block.createCuboidShape(
            5.5, 11.0, 5.5,
            10.5, 16.0, 10.5
    );

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getPipeShape(world, pos);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return getPipeShape(world, pos);
    }

    private VoxelShape getPipeShape(BlockView world, BlockPos pos) {
        VoxelShape shape = CORE_SHAPE;

        if (PipeConnections.connectsTo(world, pos, Direction.NORTH)) shape = VoxelShapes.union(shape, NORTH_ARM_SHAPE);
        if (PipeConnections.connectsTo(world, pos, Direction.SOUTH)) shape = VoxelShapes.union(shape, SOUTH_ARM_SHAPE);
        if (PipeConnections.connectsTo(world, pos, Direction.WEST))  shape = VoxelShapes.union(shape, WEST_ARM_SHAPE);
        if (PipeConnections.connectsTo(world, pos, Direction.EAST))  shape = VoxelShapes.union(shape, EAST_ARM_SHAPE);
        if (PipeConnections.connectsTo(world, pos, Direction.DOWN))  shape = VoxelShapes.union(shape, DOWN_ARM_SHAPE);
        if (PipeConnections.connectsTo(world, pos, Direction.UP))    shape = VoxelShapes.union(shape, UP_ARM_SHAPE);

        return shape;
    }
}