package com.penguin.nuclide.reaction;

import com.penguin.nuclide.tag.SpeciesTagDefinition;
import com.penguin.nuclide.tag.SpeciesTagDataLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public final class ReactionResolver {

    private ReactionResolver() {}

    public static List<ResolvedReactionParticipant> resolveInputs(
            ReactionDefinition reaction,
            Map<String, Integer> availableSpecies
    ) {
        Objects.requireNonNull(reaction, "reaction");
        Objects.requireNonNull(availableSpecies, "availableSpecies");

        List<ResolvedReactionParticipant> resolved = new ArrayList<>();

        for (ReactionParticipant input : reaction.inputs()) {
            if (input.isSpecies()) {
                int available = availableSpecies.getOrDefault(input.speciesId(), 0);
                if (available < input.count()) {
                    throw new IllegalArgumentException(
                            "Insufficient species '" + input.speciesId() +
                            "' for reaction '" + reaction.id() + "'"
                    );
                }

                resolved.add(new ResolvedReactionParticipant(
                        input,
                        input.speciesId(),
                        input.count()
                ));
                continue;
            }

            if (input.isTag()) {
                SpeciesTagDefinition tag = SpeciesTagDataLoader.TAGS.getById(input.tagId());
                if (tag == null) {
                    throw new IllegalArgumentException(
                            "Unknown tag '" + input.tagId() + "' for reaction '" + reaction.id() + "'"
                    );
                }

                String resolvedSpeciesId = null;

                for (String speciesId : tag.values()) {
                    int available = availableSpecies.getOrDefault(speciesId, 0);
                    if (available >= input.count()) {
                        resolvedSpeciesId = speciesId;
                        break;
                    }
                }

                if (resolvedSpeciesId == null) {
                    throw new IllegalArgumentException(
                            "No species in tag '" + input.tagId() +
                            "' can satisfy count " + input.count() +
                            " for reaction '" + reaction.id() + "'"
                    );
                }

                resolved.add(new ResolvedReactionParticipant(
                        input,
                        resolvedSpeciesId,
                        input.count()
                ));
                continue;
            }

            throw new IllegalArgumentException(
                    "Invalid reaction participant in reaction '" + reaction.id() + "'"
            );
        }

        return resolved;
    }
}