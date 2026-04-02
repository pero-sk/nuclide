package com.penguin.nuclide.reaction;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class ReactionExecutor {

    private ReactionExecutor() {}

    public static Map<String, Integer> execute(
            ReactionDefinition reaction,
            Map<String, Integer> availableSpecies
    ) {
        Objects.requireNonNull(reaction, "reaction");
        Objects.requireNonNull(availableSpecies, "availableSpecies");

        if (!ReactionMatcher.matches(reaction, availableSpecies)) {
            throw new IllegalArgumentException("Reaction cannot be executed: insufficient species");
        }

        Map<String, Integer> updatedSpecies = new HashMap<>(availableSpecies);

        for (ReactionParticipant input : reaction.inputs()) {
            String speciesId = input.speciesId();
            int requiredCount = input.count();

            int currentCount = updatedSpecies.getOrDefault(speciesId, 0);
            int newCount = currentCount - requiredCount;

            if (newCount <= 0) {
                updatedSpecies.remove(speciesId);
            } else {
                updatedSpecies.put(speciesId, newCount);
            }
        }

        for (ReactionParticipant output : reaction.outputs()) {
            String speciesId = output.speciesId();
            int producedCount = output.count();

            int currentCount = updatedSpecies.getOrDefault(speciesId, 0);
            updatedSpecies.put(speciesId, currentCount + producedCount);
        }

        return updatedSpecies;
    }
}