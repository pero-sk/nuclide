package com.penguin.nuclide.reaction;

import com.penguin.nuclide.data.NuclideDataLoader;

import java.util.HashSet;
import java.util.Set;

public final class ReactionValidator {

    private ReactionValidator() {}

    public static void validateOrThrow(ReactionDefinition reaction) {
        if (reaction.id().isBlank()) {
            throw new IllegalStateException("Reaction id cannot be blank");
        }

        if (reaction.inputs().isEmpty()) {
            throw new IllegalStateException("Reaction '" + reaction.id() + "' must have at least one input");
        }

        if (reaction.outputs().isEmpty()) {
            throw new IllegalStateException("Reaction '" + reaction.id() + "' must have at least one output");
        }

        if (reaction.durationTicks() < 0) {
            throw new IllegalStateException("Reaction '" + reaction.id() + "' has negative duration");
        }

        validateParticipants(reaction, reaction.inputs(), "input");
        validateParticipants(reaction, reaction.outputs(), "output");
        validateConditions(reaction);
    }

    private static void validateParticipants(
            ReactionDefinition reaction,
            java.util.List<ReactionParticipant> participants,
            String side
    ) {
        Set<String> seen = new HashSet<>();

        for (ReactionParticipant participant : participants) {
            if (participant.speciesId().isBlank()) {
                throw new IllegalStateException(
                        "Reaction '" + reaction.id() + "' has blank " + side + " species id"
                );
            }

            if (participant.count() <= 0) {
                throw new IllegalStateException(
                        "Reaction '" + reaction.id() + "' has non-positive " + side +
                        " count for species '" + participant.speciesId() + "'"
                );
            }

            if (!NuclideDataLoader.SPECIES.containsId(participant.speciesId())) {
                throw new IllegalStateException(
                        "Reaction '" + reaction.id() + "' references unknown " + side +
                        " species '" + participant.speciesId() + "'"
                );
            }

            if (!seen.add(participant.speciesId())) {
                throw new IllegalStateException(
                        "Reaction '" + reaction.id() + "' contains duplicate " + side +
                        " species '" + participant.speciesId() + "'"
                );
            }
        }
    }

    private static void validateConditions(ReactionDefinition reaction) {
        ReactionConditions conditions = reaction.conditions();

        if (conditions.minTemperature() != null && conditions.maxTemperature() != null) {
            if (conditions.minTemperature() > conditions.maxTemperature()) {
                throw new IllegalStateException(
                        "Reaction '" + reaction.id() + "' has min_temperature greater than max_temperature"
                );
            }
        }

        if (conditions.hasCatalyst()) {
            if (!NuclideDataLoader.SPECIES.containsId(conditions.catalystSpeciesId())) {
                throw new IllegalStateException(
                        "Reaction '" + reaction.id() + "' references unknown catalyst species '" +
                                conditions.catalystSpeciesId() + "'"
                );
            }
        }
    }
}