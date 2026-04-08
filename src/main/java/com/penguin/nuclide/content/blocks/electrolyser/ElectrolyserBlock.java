package com.penguin.nuclide.content.blocks.electrolyser;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.penguin.nuclide.content.blockentities.electrolyser.ElectrolyserBlockEntity;
import com.penguin.nuclide.content.registry.ModBlockEntities;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ElectrolyserBlock extends BlockWithEntity {

    public ElectrolyserBlock(Settings settings) {
        super(settings);
    }

    public static final MapCodec<ElectrolyserBlock> CODEC = createCodec(ElectrolyserBlock::new);

    @Override
    public BlockEntity createBlockEntity(BlockPos arg0, BlockState arg1) {
        return new ElectrolyserBlockEntity(arg0, arg1);
    }

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return CODEC;
    }
    
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return validateTicker(
            type,
            ModBlockEntities.ELECTROLYSER,
            ElectrolyserBlockEntity::tick
        );
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

}
