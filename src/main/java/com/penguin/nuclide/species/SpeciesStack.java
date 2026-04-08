package com.penguin.nuclide.species;

import java.util.Objects;

import com.penguin.nuclide.atomic.StateType;
import com.penguin.nuclide.data.NuclideDataLoader;
import com.penguin.nuclide.data.SpeciesDefinition;

public final class SpeciesStack {
    private final String speciesId;
    private final int count;
    private final StateType state;

    public SpeciesStack(String speciesId, int count, StateType state) {
        this.speciesId = Objects.requireNonNull(speciesId, "speciesId");
        this.count = count;
        this.state = state;

        if (speciesId.isBlank()) {
            throw new IllegalArgumentException("speciesId cannot be blank");
        }

        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
    }

    public SpeciesStack(SpeciesKey key, int count) {
        this.speciesId = Objects.requireNonNull(key.speciesId(), "speciesId");
        this.count = count;
        this.state = key.currentState();

        if (speciesId.isBlank()) {
            throw new IllegalArgumentException("speciesId cannot be blank");
        }

        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
    }

    public SpeciesStack(String speciesId, int count) {
        this.speciesId = Objects.requireNonNull(speciesId, "speciesId");
        this.count = count;
        this.state = NuclideDataLoader.SPECIES.getById(speciesId).state();

        if (speciesId.isBlank()) {
            throw new IllegalArgumentException("speciesId cannot be blank");
        }

        if (count < 0) {
            throw new IllegalArgumentException("count cannot be negative");
        }
    }

    public String speciesId() {
        return speciesId;
    }

    public int count() {
        return count;
    }

    public SpeciesKey key() {
        return new SpeciesKey(speciesId, state);
    }

    public boolean isEmpty() {
        return count == 0;
    }

    public StateType state() {
        return state;
    }

    public SpeciesDefinition definition() {
        return NuclideDataLoader.SPECIES.getById(speciesId);
    }

    public SpeciesStack withCount(int newCount) {
        return new SpeciesStack(speciesId, newCount);
    }

    public SpeciesStack grow(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("grow amount cannot be negative");
        }
        int newCount = Math.addExact(count, amount);
        return new SpeciesStack(speciesId, newCount);
    }

    public SpeciesStack shrink(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("shrink amount cannot be negative");
        }

        int newCount = count - amount;
        if (newCount < 0) {
            throw new IllegalArgumentException(
                    "Cannot shrink stack of " + speciesId + " below zero"
            );
        }

        return new SpeciesStack(speciesId, newCount);
    }

    public static SpeciesStack empty(String speciesId) {
        return new SpeciesStack(speciesId, 0);
    }

    public boolean sameSpecies(String otherSpeciesId) {
        return speciesId.equals(otherSpeciesId);
    }

    public boolean sameSpecies(SpeciesStack other) {
        return other != null && speciesId.equals(other.speciesId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpeciesStack that)) return false;
        return count == that.count && speciesId.equals(that.speciesId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(speciesId, count);
    }

    @Override
    public String toString() {
        return "SpeciesStack{" +
                "speciesId='" + speciesId + '\'' +
                ", count=" + count +
                '}';
    }
}