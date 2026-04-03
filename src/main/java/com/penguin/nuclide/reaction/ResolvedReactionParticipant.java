package com.penguin.nuclide.reaction;

import java.util.Objects;

public final class ResolvedReactionParticipant {
    private final ReactionParticipant original;
    private final String resolvedSpeciesId;
    private final int count;

    public ResolvedReactionParticipant(
            ReactionParticipant original,
            String resolvedSpeciesId,
            int count
    ) {
        this.original = Objects.requireNonNull(original);
        this.resolvedSpeciesId = Objects.requireNonNull(resolvedSpeciesId);
        this.count = count;
    }

    public ReactionParticipant original() {
        return original;
    }

    public String resolvedSpeciesId() {
        return resolvedSpeciesId;
    }

    public int count() {
        return count;
    }

    @Override
    public String toString() {
        return "ResolvedReactionParticipant{" +
                "original=" + original +
                ", resolvedSpeciesId='" + resolvedSpeciesId + '\'' +
                ", count=" + count +
                '}';
    }
}