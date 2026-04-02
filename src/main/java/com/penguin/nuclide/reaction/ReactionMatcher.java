package com.penguin.nuclide.reaction;

import java.util.Map;
import java.util.Objects;

public final class ReactionMatcher {

    private ReactionMatcher() {}

    public static boolean matches(ReactionDefinition reaction, Map<String, Integer> availableSpecies) {
        return matches(reaction, availableSpecies, ReactionContext.DEFAULT);
    }

    public static boolean matches(
            ReactionDefinition reaction,
            Map<String, Integer> availableSpecies,
            ReactionContext context
    ) {
        Objects.requireNonNull(reaction, "reaction");
        Objects.requireNonNull(availableSpecies, "availableSpecies");
        Objects.requireNonNull(context, "context");

        for (ReactionParticipant input : reaction.inputs()) {
            String speciesId = input.speciesId();
            int requiredCount = input.count();

            int availableCount = availableSpecies.getOrDefault(speciesId, 0);
            if (availableCount < requiredCount) {
                return false;
            }
        }

        ReactionConditions conditions = reaction.conditions();

        if (conditions.minTemperature() != null && context.temperature() < conditions.minTemperature()) {
            return false;
        }

        if (conditions.maxTemperature() != null && context.temperature() > conditions.maxTemperature()) {
            return false;
        }

        if (conditions.requiresSpark() && !context.hasSpark()) {
            return false;
        }

        if (conditions.hasCatalyst()) {
            int catalystCount = availableSpecies.getOrDefault(conditions.catalystSpeciesId(), 0);
            if (catalystCount <= 0) {
                return false;
            }
        }

        return true;
    }
}