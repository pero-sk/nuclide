package com.penguin.nuclide.reaction;

import java.util.Objects;

import com.penguin.nuclide.species.SpeciesKey;

public final class ResolvedReactionParticipant {
    private final ReactionParticipant original;
    private final SpeciesKey resolvedKey;
    private final int count;

    public ResolvedReactionParticipant(
            ReactionParticipant original,
            SpeciesKey resolvedKey,
            int count
    ) {
        this.original = Objects.requireNonNull(original);
        this.resolvedKey = Objects.requireNonNull(resolvedKey);
        this.count = count;
    }

    public ReactionParticipant original() {
        return original;
    }

    public SpeciesKey resolvedKey() {
        return resolvedKey;
    }

    public String resolvedSpeciesId() {
        return resolvedKey.speciesId();
    }

    public int count() {
        return count;
    }

    @Override
    public String toString() {
        return "ResolvedReactionParticipant{" +
                "original=" + original +
                ", resolvedKey=" + resolvedKey +
                ", count=" + count +
                '}';
    }
}