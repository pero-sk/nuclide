package com.penguin.nuclide.reaction;

import java.util.Objects;

public final class ReactionParticipant {
    private final String speciesId;
    private final String tagId;
    private final int count;

    public ReactionParticipant(String speciesId, String tagId, int count) {
        this.speciesId = speciesId;
        this.tagId = tagId;
        this.count = count;
    }

    public static ReactionParticipant species(String speciesId, int count) {
        return new ReactionParticipant(Objects.requireNonNull(speciesId), null, count);
    }

    public static ReactionParticipant tag(String tagId, int count) {
        return new ReactionParticipant(null, Objects.requireNonNull(tagId), count);
    }

    public String speciesId() {
        return speciesId;
    }

    public String tagId() {
        return tagId;
    }

    public int count() {
        return count;
    }

    public boolean isSpecies() {
        return speciesId != null && !speciesId.isBlank();
    }

    public boolean isTag() {
        return tagId != null && !tagId.isBlank();
    }

    @Override
    public String toString() {
        return "ReactionParticipant{" +
                "speciesId='" + speciesId + '\'' +
                ", tagId='" + tagId + '\'' +
                ", count=" + count +
                '}';
    }
}