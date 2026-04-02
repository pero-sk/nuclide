package com.penguin.nuclide.reaction;

import java.util.Objects;

public final class ReactionParticipant {
    private final String speciesId;
    private final int count;

    public ReactionParticipant(String speciesId, int count) {
        this.speciesId = Objects.requireNonNull(speciesId);
        this.count = count;
    }

    public String speciesId() {
        return speciesId;
    }

    public int count() {
        return count;
    }

    @Override
    public String toString() {
        return "ReactionParticipant{" +
                "speciesId='" + speciesId + '\'' +
                ", count=" + count +
                '}';
    }
}