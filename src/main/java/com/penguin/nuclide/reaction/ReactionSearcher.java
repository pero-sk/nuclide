package com.penguin.nuclide.reaction;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import com.penguin.nuclide.species.SpeciesContainer;

public final class ReactionSearcher {

    private ReactionSearcher() {}

    public static List<ReactionDefinition> findMatches(
            Iterable<ReactionDefinition> reactions,
            SpeciesContainer availableSpecies,
            boolean checkConditions
    ) {
        Objects.requireNonNull(reactions, "reactions");
        Objects.requireNonNull(availableSpecies, "availableSpecies");

        List<ReactionDefinition> matches = new ArrayList<>();

        for (ReactionDefinition reaction : reactions) {
            boolean matched = checkConditions
                    ? ReactionMatcher.matches(reaction, availableSpecies, ReactionContext.DEFAULT)
                    : ReactionMatcher.matchesSpeciesOnly(reaction, availableSpecies);

            if (matched) {
                matches.add(reaction);
            }
        }

        matches.sort(Comparator.comparing(ReactionDefinition::id));
        return matches;
    }

    public static List<ReactionDefinition> findMatches(
            Iterable<ReactionDefinition> reactions,
            SpeciesContainer availableSpecies,
            ReactionContext context
    ) {
        Objects.requireNonNull(reactions, "reactions");
        Objects.requireNonNull(availableSpecies, "availableSpecies");
        Objects.requireNonNull(context, "context");

        List<ReactionDefinition> matches = new ArrayList<>();

        for (ReactionDefinition reaction : reactions) {
            if (ReactionMatcher.matches(reaction, availableSpecies, context)) {
                matches.add(reaction);
            }
        }

        matches.sort(Comparator.comparing(ReactionDefinition::id));
        return matches;
    }
}