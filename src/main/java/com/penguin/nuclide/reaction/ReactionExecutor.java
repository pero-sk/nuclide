package com.penguin.nuclide.reaction;

import java.util.List;
import java.util.Objects;

import com.penguin.nuclide.data.NuclideDataLoader;
import com.penguin.nuclide.data.SpeciesDefinition;
import com.penguin.nuclide.atomic.StateType;
import com.penguin.nuclide.species.SpeciesContainer;

public final class ReactionExecutor {

    private ReactionExecutor() {}

    public static SpeciesContainer execute(
            ReactionDefinition reaction,
            SpeciesContainer availableSpecies
    ) {
        return execute(reaction, availableSpecies, ReactionContext.DEFAULT);
    }

    public static SpeciesContainer execute(
            ReactionDefinition reaction,
            SpeciesContainer availableSpecies,
            ReactionContext context
    ) {
        Objects.requireNonNull(reaction, "reaction");
        Objects.requireNonNull(availableSpecies, "availableSpecies");
        Objects.requireNonNull(context, "context");

        if (!ReactionMatcher.matches(reaction, availableSpecies, context)) {
            throw new IllegalArgumentException(
                    "Reaction cannot be executed: insufficient species or unmet conditions"
            );
        }

        List<ResolvedReactionParticipant> resolvedInputs =
                ReactionResolver.resolveInputs(reaction, availableSpecies);

        SpeciesContainer updatedSpecies = availableSpecies.copy();

        for (ResolvedReactionParticipant input : resolvedInputs) {
            updatedSpecies.remove(input.resolvedKey(), input.count());
        }

        for (ReactionParticipant output : reaction.outputs()) {
            if (!output.isSpecies()) {
                throw new UnsupportedOperationException(
                        "ReactionExecutor v1 only supports exact-species outputs, not tag outputs"
                );
            }

            SpeciesDefinition species = NuclideDataLoader.SPECIES.getById(output.speciesId());
            if (species == null) {
                throw new IllegalStateException("Unknown output species: " + output.speciesId());
            }

            StateType state = species.state();
            updatedSpecies.add(output.speciesId(), state, output.count());
        }

        return updatedSpecies;
    }
}