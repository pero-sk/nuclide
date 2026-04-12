package com.penguin.nuclide.reaction;

import com.penguin.nuclide.data.NuclideDataLoader;
import com.penguin.nuclide.tag.SpeciesTagDataLoader;

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
            if (participant.count() <= 0) {
                throw new IllegalStateException(
                        "Reaction '" + reaction.id() + "' has non-positive " + side + " count"
                );
            }

            if (participant.isSpecies() == participant.isTag()) {
                throw new IllegalStateException(
                        "Reaction '" + reaction.id() + "' must define exactly one of species or tag for a " + side
                );
            }

            String key;
            if (participant.isSpecies()) {
                if (!NuclideDataLoader.SPECIES.containsId(participant.speciesId())) {
                    throw new IllegalStateException(
                            "Reaction '" + reaction.id() + "' references unknown " + side +
                                    " species '" + participant.speciesId() + "'"
                    );
                }
                key = "species:" + participant.speciesId();
            } else {
                if (!SpeciesTagDataLoader.TAGS.containsId(participant.tagId())) {
                    throw new IllegalStateException(
                            "Reaction '" + reaction.id() + "' references unknown " + side +
                                    " tag '" + participant.tagId() + "'"
                    );
                }
                key = "tag:" + participant.tagId();
            }

            if (!seen.add(key)) {
                throw new IllegalStateException(
                        "Reaction '" + reaction.id() + "' contains duplicate " + side + " participant '" + key + "'"
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