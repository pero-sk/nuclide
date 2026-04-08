package com.penguin.nuclide.content.blockentities.binder;

import com.penguin.nuclide.content.blockentities.base.AbstractReactionMachineBlockEntity;
import com.penguin.nuclide.content.registry.ModBlockEntities;
import com.penguin.nuclide.reaction.ReactionDataLoader;
import com.penguin.nuclide.reaction.ReactionDefinition;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BinderBlockEntity extends AbstractReactionMachineBlockEntity {

    public BinderBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BINDER, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, BinderBlockEntity binder) {
        if (world.isClient) {
            return;
        }

        binder.tickServer();
    }

    @Override
    protected Iterable<ReactionDefinition> getCandidateReactions() {
        return ReactionDataLoader.REACTIONS.values();
    }

    @Override
    protected boolean supportsReaction(ReactionDefinition reaction) {
        return reaction.id().contains(":binder_recombination_");
    }
}