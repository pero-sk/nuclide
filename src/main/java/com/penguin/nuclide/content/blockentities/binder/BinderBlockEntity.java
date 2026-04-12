package com.penguin.nuclide.content.blockentities.binder;

import java.util.ArrayList;
import java.util.List;

import com.penguin.nuclide.content.blockentities.base.AbstractReactionMachineBlockEntity;
import com.penguin.nuclide.content.registry.ModBlockEntities;
import com.penguin.nuclide.data.NuclideDataLoader;
import com.penguin.nuclide.data.SpeciesDefinition;
import com.penguin.nuclide.reaction.ReactionDataLoader;
import com.penguin.nuclide.reaction.ReactionDefinition;
import com.penguin.nuclide.species.SpeciesContainer;
import com.penguin.nuclide.species.SpeciesKey;
import com.penguin.nuclide.species.SpeciesStack;
import com.penguin.nuclide.misc.IHaveHoverInformation;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BinderBlockEntity extends AbstractReactionMachineBlockEntity implements IHaveHoverInformation {

    // hydroxyl is still waste here
    private static final String HYDROXYL_ID = "molecules:hydroxyl";

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

    @Override
    public boolean addHoverInformation(
            World world,
            BlockHitResult hit,
            PlayerEntity player,
            List<Text> tooltip
    ) {
        tooltip.add(Text.literal("Binder"));

        if (getActiveReaction() != null) {
            tooltip.add(Text.literal("Reaction: " + getActiveReaction().name()));
            tooltip.add(Text.literal("Progress: " + getProgress() + " / " + getActiveReaction().durationTicks()));
        } else {
            tooltip.add(Text.literal("Reaction: idle"));
        }

        if (getInputContainer().isEmpty()) {
            tooltip.add(Text.literal("Input: empty"));
        } else {
            tooltip.add(Text.literal("Input:"));
            for (SpeciesStack stack : getInputContainer().stacks()) {
                SpeciesDefinition definition = NuclideDataLoader.SPECIES.getById(stack.speciesId());
                String name = definition != null ? definition.name() : stack.speciesId();
                tooltip.add(Text.literal("- " + name + ": " + stack.count() + " mmol"));
            }
        }

        if (getOutputContainer().isEmpty()) {
            tooltip.add(Text.literal("Output: empty"));
        } else {
            tooltip.add(Text.literal("Output:"));
            for (SpeciesStack stack : getOutputContainer().stacks()) {
                SpeciesDefinition definition = NuclideDataLoader.SPECIES.getById(stack.speciesId());
                String name = definition != null ? definition.name() : stack.speciesId();
                tooltip.add(Text.literal("- " + name + ": " + stack.count() + " mmol"));
            }
        }

        for (String line : getInefficiencyBreakdownLines()) {
            tooltip.add(Text.literal(line));
        }

        return true;
    }

    private int getWastePenaltyDivisor() {
        return 200; // mmols
    }

    private int getWastePenaltyMax() {
        return 5; // P
    }

    private int getWastePenaltyP() {
        int totalHydroxyl = countSpecies(input, HYDROXYL_ID) + countSpecies(output, HYDROXYL_ID);
        return Math.min(getWastePenaltyMax(), totalHydroxyl / getWastePenaltyDivisor());
    }

    private int countSpecies(SpeciesContainer container, String speciesId) {
        int total = 0;

        for (SpeciesKey key : container.asMap().keySet()) {
            if (speciesId.equals(key.speciesId())) {
                total += container.countOf(key);
            }
        }

        return total;
    }

    @Override
    protected List<InefficiencyEntry> getAdditionalInefficiencyEntries() {
        List<InefficiencyEntry> entries = new ArrayList<>();

        int wastePenalty = getWastePenaltyP();
        if (wastePenalty > 0) {
            entries.add(new InefficiencyEntry("Waste penalty", wastePenalty));
        }

        return entries;
    }
}