package com.penguin.nuclide.reaction;

import com.penguin.nuclide.species.SpeciesContainer;
import com.penguin.nuclide.tag.SpeciesTagDataLoader;
import com.penguin.nuclide.tag.SpeciesTagDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ReactionResolver {

    private ReactionResolver() {}

    public static List<ResolvedReactionParticipant> resolveInputs(
            ReactionDefinition reaction,
            SpeciesContainer availableSpecies
    ) {
        Objects.requireNonNull(reaction, "reaction");
        Objects.requireNonNull(availableSpecies, "availableSpecies");

        SpeciesContainer remaining = availableSpecies.copy();
        List<ResolvedReactionParticipant> resolved = new ArrayList<>();

        for (ReactionParticipant input : reaction.inputs()) {
            if (input.isSpecies()) {
                String speciesId = input.speciesId();

                if (!remaining.contains(speciesId, input.count())) {
                    throw new IllegalArgumentException(
                            "Insufficient species '" + speciesId +
                            "' for reaction '" + reaction.id() + "'"
                    );
                }

                remaining.remove(speciesId, input.count());
                resolved.add(new ResolvedReactionParticipant(
                        input,
                        speciesId,
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
                    if (remaining.contains(speciesId, input.count())) {
                        resolvedSpeciesId = speciesId;
                        remaining.remove(speciesId, input.count());
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