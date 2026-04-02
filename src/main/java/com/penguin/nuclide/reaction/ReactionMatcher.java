package com.penguin.nuclide.reaction;

import com.penguin.nuclide.tag.SpeciesTagDefinition;
import com.penguin.nuclide.tag.SpeciesTagDataLoader;

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

        if (!matchesSpeciesOnly(reaction, availableSpecies)) {
            return false;
        }

        ReactionConditions conditions = reaction.conditions();

        if (conditions.minTemperature() != null && context.temperature() < conditions.minTemperature()) {
            return false;
        }

        if (conditions.maxTemperature() != null && context.temperature() > conditions.maxTemperature()) {
            return false;
        }

        if (conditions.minPressure() != null && context.pressure() < conditions.minPressure()) {
            return false;
        }

        if (conditions.maxPressure() != null && context.pressure() > conditions.maxPressure()) {
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

    public static boolean matchesSpeciesOnly(
            ReactionDefinition reaction,
            Map<String, Integer> availableSpecies
    ) {
        Objects.requireNonNull(reaction, "reaction");
        Objects.requireNonNull(availableSpecies, "availableSpecies");

        for (ReactionParticipant input : reaction.inputs()) {
            if (input.isSpecies()) {
                int availableCount = availableSpecies.getOrDefault(input.speciesId(), 0);
                if (availableCount < input.count()) {
                    return false;
                }
            } else if (input.isTag()) {
                SpeciesTagDefinition tag = SpeciesTagDataLoader.TAGS.getById(input.tagId());
                if (tag == null) {
                    return false;
                }

                boolean satisfied = false;
                for (String speciesId : tag.values()) {
                    int availableCount = availableSpecies.getOrDefault(speciesId, 0);
                    if (availableCount >= input.count()) {
                        satisfied = true;
                        break;
                    }
                }

                if (!satisfied) {
                    return false;
                }
            } else {
                return false;
            }
        }

        return true;
    }
}