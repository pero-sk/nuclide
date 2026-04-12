package com.penguin.nuclide.reaction;

import java.util.Objects;

import com.penguin.nuclide.species.SpeciesContainer;
import com.penguin.nuclide.species.SpeciesStack;

public final class ReactionMatcher {

    private ReactionMatcher() {}

    public static boolean matches(ReactionDefinition reaction, SpeciesContainer availableSpecies) {
        return matches(reaction, availableSpecies, ReactionContext.DEFAULT);
    }

    public static boolean matches(
            ReactionDefinition reaction,
            SpeciesContainer availableSpecies,
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
            boolean foundCatalyst = false;

            for (SpeciesStack stack : availableSpecies.stacks()) {
                if (stack.speciesId().equals(conditions.catalystSpeciesId()) && stack.count() > 0) {
                    foundCatalyst = true;
                    break;
                }
            }

            if (!foundCatalyst) {
                return false;
            }
        }

        return true;
    }

    public static boolean matchesSpeciesOnly(
            ReactionDefinition reaction,
            SpeciesContainer availableSpecies
    ) {
        Objects.requireNonNull(reaction, "reaction");
        Objects.requireNonNull(availableSpecies, "availableSpecies");

        try {
            ReactionResolver.resolveInputs(reaction, availableSpecies);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}