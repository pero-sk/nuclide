package com.penguin.nuclide.content.blocks.creative.energiser;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;
import com.penguin.nuclide.content.blockentities.creative.energiser.CreativeEnergiserBlockEntity;
import com.penguin.nuclide.content.registry.ModBlockEntities;

import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CreativeEnergiserBlock extends BlockWithEntity {

    MapCodec<CreativeEnergiserBlock> CODEC = createCodec(CreativeEnergiserBlock::new);

    public CreativeEnergiserBlock(Settings settings) {
        super(settings);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos arg0, BlockState arg1) {
        return new CreativeEnergiserBlockEntity(arg0, arg1);
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
            ModBlockEntities.CREATIVE_ENERGISER,
            CreativeEnergiserBlockEntity::tick
        );
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
}
