package com.penguin.nuclide.content.blocks.hydrogen_furnace;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.penguin.nuclide.content.blockentities.hydrogen_furnace.HydrogenFurnaceBlockEntity;
import com.penguin.nuclide.content.registry.ModBlockEntities;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HydrogenFurnaceBlock extends BlockWithEntity {

    public static final MapCodec<HydrogenFurnaceBlock> CODEC = createCodec(HydrogenFurnaceBlock::new);

    public HydrogenFurnaceBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos arg0, BlockState arg1) {
        return new HydrogenFurnaceBlockEntity(arg0, arg1);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }
    

    @Override
    protected ActionResult onUse(
            BlockState state,
            World world,
            BlockPos pos,
            PlayerEntity player,
            BlockHitResult hit
    ) {
        if (world.isClient) {return ActionResult.FAIL;}

        BlockEntity be = world.getBlockEntity(pos);
        if (!(be instanceof HydrogenFurnaceBlockEntity furnace)) {return ActionResult.FAIL;}

        player.sendMessage(Text.literal(
            "Energy: " + furnace.getStoredEnergy() + "\n" +
            "Progress: " + furnace.getProgress() + "\n"
        ));

        return ActionResult.SUCCESS_NO_ITEM_USED;
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(type, ModBlockEntities.HYDROGEN_FURNACE, HydrogenFurnaceBlockEntity::tick);
    }
}
