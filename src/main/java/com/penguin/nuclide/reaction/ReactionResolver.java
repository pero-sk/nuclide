package com.penguin.nuclide.reaction;

import com.penguin.nuclide.species.SpeciesContainer;
import com.penguin.nuclide.species.SpeciesStack;
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
                SpeciesStack matched = findMatchingSpeciesStack(remaining, input.speciesId(), input.count());

                if (matched == null) {
                    throw new IllegalArgumentException(
                            "Insufficient species '" + input.speciesId() +
                            "' for reaction '" + reaction.id() + "'"
                    );
                }

                remaining.remove(matched.key(), input.count());
                resolved.add(new ResolvedReactionParticipant(
                        input,
                        matched.key(),
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

                SpeciesStack matched = null;

                for (String speciesId : tag.values()) {
                    matched = findMatchingSpeciesStack(remaining, speciesId, input.count());
                    if (matched != null) {
                        remaining.remove(matched.key(), input.count());
                        break;
                    }
                }

                if (matched == null) {
                    throw new IllegalArgumentException(
                            "No species in tag '" + input.tagId() +
                            "' can satisfy count " + input.count() +
                            " for reaction '" + reaction.id() + "'"
                    );
                }

                resolved.add(new ResolvedReactionParticipant(
                        input,
                        matched.key(),
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

    private static SpeciesStack findMatchingSpeciesStack(
            SpeciesContainer container,
            String speciesId,
            int requiredCount
    ) {
        for (SpeciesStack stack : container.stacks()) {
            if (!stack.speciesId().equals(speciesId)) {
                continue;
            }

            if (stack.count() >= requiredCount) {
                return stack;
            }
        }

        return null;
    }
}