package com.penguin.nuclide.reaction;

import java.util.List;
import java.util.Objects;

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
            updatedSpecies.remove(input.resolvedSpeciesId(), input.count());
        }

        for (ReactionParticipant output : reaction.outputs()) {
            if (!output.isSpecies()) {
                throw new UnsupportedOperationException(
                        "ReactionExecutor v1 only supports exact-species outputs, not tag outputs"
                );
            }

            updatedSpecies.add(output.speciesId(), output.count());
        }

        return updatedSpecies;
    }
}