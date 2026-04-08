package com.penguin.nuclide.content.blocks.species_tank;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.penguin.nuclide.Nuclide;
import com.penguin.nuclide.content.blockentities.species_tank.SpeciesTankCasingBlockEntity;
import com.penguin.nuclide.content.blockentities.species_tank.SpeciesTankControllerBlockEntity;
import com.penguin.nuclide.content.items.CanisterItem;
import com.penguin.nuclide.nbt.NuclideDataComponents;
import com.penguin.nuclide.species.SpeciesContainer;

import com.penguin.nuclide.misc.TankHalfX;
import com.penguin.nuclide.misc.TankHalfZ;
import com.penguin.nuclide.misc.TankLayer;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.LivingEntity;
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

public class SpeciesTankCasingBlock extends BlockWithEntity {

    public static final MapCodec<SpeciesTankCasingBlock> CODEC =
            createCodec(SpeciesTankCasingBlock::new);

    public static final EnumProperty<TankHalfX> HALF_X =
            EnumProperty.of("half_x", TankHalfX.class);

    public static final EnumProperty<TankHalfZ> HALF_Z =
            EnumProperty.of("half_z", TankHalfZ.class);

    public static final EnumProperty<TankLayer> LAYER =
            EnumProperty.of("layer", TankLayer.class);

    public static final BooleanProperty RENDER_PULSE = BooleanProperty.of("render_pulse");

    public SpeciesTankCasingBlock(Settings settings) {
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
    protected void appendProperties(StateManager.Builder<net.minecraft.block.Block, BlockState> builder) {
        builder.add(HALF_X, HALF_Z, LAYER, RENDER_PULSE);
    }


    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new com.penguin.nuclide.content.blockentities.species_tank.SpeciesTankCasingBlockEntity(pos, state);
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
        if (!(be instanceof SpeciesTankCasingBlockEntity casing)) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        SpeciesTankControllerBlockEntity controller = casing.getController();
        if (controller == null) {
            Nuclide.LOGGER.error("LINK CONTROLLER AMOUNTED TO NONE.");
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
        if (be instanceof SpeciesTankCasingBlockEntity casing) {
            SpeciesTankControllerBlockEntity controller = casing.getController();
            if (controller != null) {
                player.sendMessage(
                        controller.getDisplayText(),
                        false
                );
                return ActionResult.SUCCESS;
            }
        }

        return ActionResult.PASS;
    }

    @Override
    public void onPlaced(
            World world,
            BlockPos pos,
            BlockState state,
            @Nullable LivingEntity placer,
            ItemStack itemStack
    ) {
        super.onPlaced(world, pos, state, placer, itemStack);

        if (world.isClient) {
            return;
        }

        tryFormNearby(world, pos);
    }

    @Override
    public void onBlockAdded(
            BlockState state,
            World world,
            BlockPos pos,
            BlockState oldState,
            boolean notify
    ) {
        super.onBlockAdded(state, world, pos, oldState, notify);

        if (world.isClient) {
            return;
        }

        if (state.isOf(oldState.getBlock())) {
            return;
        }

        tryFormNearby(world, pos);
    }

    private static void tryFormNearby(World world, BlockPos pos) {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (SpeciesTankControllerBlockEntity.tryFormFromAnyMember(world, pos.add(dx, 0, dz))) {
                    return;
                }
            }
        }
    }
}