package com.penguin.nuclide.content.blocks.pump;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.penguin.nuclide.content.blockentities.pump.PumpBlockEntity;
import com.penguin.nuclide.content.registry.ModBlockEntities;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.function.BooleanBiFunction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class PumpBlock extends BlockWithEntity {
    public static final MapCodec<PumpBlock> CODEC = createCodec(PumpBlock::new);

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE_NORTH = makeNorthShape();
    private static final VoxelShape SHAPE_EAST = rotateY(SHAPE_NORTH);
    private static final VoxelShape SHAPE_SOUTH = rotateY(SHAPE_EAST);
    private static final VoxelShape SHAPE_WEST = rotateY(SHAPE_SOUTH);

    public PumpBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(Properties.HORIZONTAL_FACING);
    }

    public BlockState getFacing() {
        return getStateManager().getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.NORTH);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(Properties.HORIZONTAL_FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PumpBlockEntity(pos, state);
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
                ModBlockEntities.PUMP,
                PumpBlockEntity::tick
        );
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case NORTH -> SHAPE_NORTH;
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    private static VoxelShape makeNorthShape() {
        return VoxelShapes.union(
                // main body
                cuboid(4.5, 4.5, 4.5, 11.5, 11.5, 11.5),

                // simplified valve
                cuboid(5.5, 10.5, 5.5, 10.5, 15.5, 10.5),

                // front arm (north)
                cuboid(5.5, 5.0, 0.0, 10.5, 10.0, 4.5),

                // back arm (south)
                cuboid(6.0, 6.0, 11.5, 10.0, 10.0, 16.0)
        );
    }

    private static VoxelShape cuboid(double minX, double minY, double minZ, double maxX, double maxY, double maxZ) {
        return Block.createCuboidShape(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static VoxelShape rotateY(VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[] { shape, VoxelShapes.empty() };

        for (var box : shape.getBoundingBoxes()) {
            buffer[1] = VoxelShapes.combineAndSimplify(
                    buffer[1],
                    VoxelShapes.cuboid(
                            1.0 - box.maxZ,
                            box.minY,
                            box.minX,
                            1.0 - box.minZ,
                            box.maxY,
                            box.maxX
                    ),
                    BooleanBiFunction.OR
            );
        }

        return buffer[1];
    }
}