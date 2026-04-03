package com.penguin.nuclide.tag;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

public final class SpeciesTagDefinition {
    private final String id;
    private final Set<String> values = new LinkedHashSet<>();

    public SpeciesTagDefinition(String id) {
        this.id = Objects.requireNonNull(id);
    }

    public String id() {
        return id;
    }

    public void add(String speciesId) {
        values.add(Objects.requireNonNull(speciesId));
    }

    public void addAll(Iterable<String> speciesIds) {
        for (String speciesId : speciesIds) {
            add(speciesId);
        }
    }

    public boolean contains(String speciesId) {
        return values.contains(speciesId);
    }

    public Set<String> values() {
        return Collections.unmodifiableSet(values);
    }

    public int size() {
        return values.size();
    }

    @Override
    public String toString() {
        return "SpeciesTagDefinition{" +
                "id='" + id + '\'' +
                ", values=" + values +
                '}';
    }
}