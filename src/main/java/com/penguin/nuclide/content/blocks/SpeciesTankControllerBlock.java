package com.penguin.nuclide.content.blocks;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.penguin.nuclide.content.blockentities.SpeciesTankControllerBlockEntity;
import com.penguin.nuclide.content.items.CanisterItem;
import com.penguin.nuclide.content.registry.ModBlockEntities;
import com.penguin.nuclide.nbt.NuclideDataComponents;
import com.penguin.nuclide.species.SpeciesContainer;

import com.penguin.nuclide.misc.TankHalfX;
import com.penguin.nuclide.misc.TankHalfZ;
import com.penguin.nuclide.misc.TankLayer;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpeciesTankControllerBlock extends BlockWithEntity {

    public static final MapCodec<SpeciesTankControllerBlock> CODEC =
            createCodec(SpeciesTankControllerBlock::new);
    
    public static final EnumProperty<TankHalfX> HALF_X =
            EnumProperty.of("half_x", TankHalfX.class);

    public static final EnumProperty<TankHalfZ> HALF_Z =
            EnumProperty.of("half_z", TankHalfZ.class);

    public static final EnumProperty<TankLayer> LAYER =
            EnumProperty.of("layer", TankLayer.class);

    public static final BooleanProperty RENDER_PULSE = BooleanProperty.of("render_pulse");

    public SpeciesTankControllerBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(HALF_X, TankHalfX.WEST)
                .with(HALF_Z, TankHalfZ.NORTH)
                .with(LAYER, TankLayer.SINGLE)
                .with(RENDER_PULSE, true));
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<net.minecraft.block.Block, BlockState> builder) {
        builder.add(HALF_X, HALF_Z, LAYER, RENDER_PULSE);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new SpeciesTankControllerBlockEntity(pos, state);
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
                ModBlockEntities.SPECIES_TANK_CONTROLLER,
                SpeciesTankControllerBlockEntity::tick
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

        if (!(stack.getItem() instanceof CanisterItem)) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof SpeciesTankControllerBlockEntity controller)) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        SpeciesContainer canisterContainer =
                stack.getOrDefault(
                        NuclideDataComponents.SPECIES_CONTAINER,
                        new SpeciesContainer()
                );

        if (canisterContainer.isEmpty()) {
            player.sendMessage(Text.literal("canister is empty"), false);
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        int inserted = controller.insertFromContainer(canisterContainer);

        if (inserted > 0) {
            CanisterItem.setContainer(stack, canisterContainer);

            player.sendMessage(
                    Text.literal("Inserted " + inserted + " mmol"),
                    false
            );

            return ItemActionResult.SUCCESS;
        }

        return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
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

        if (!player.getMainHandStack().isEmpty()) {
            return ActionResult.PASS;
        }

        BlockEntity be = world.getBlockEntity(pos);
        if (be instanceof SpeciesTankControllerBlockEntity controller) {
            player.sendMessage(
                    controller.getDisplayText(),
                    false
            );
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }
}