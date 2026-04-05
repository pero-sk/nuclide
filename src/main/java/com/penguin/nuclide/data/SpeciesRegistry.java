package com.penguin.nuclide.data;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class SpeciesRegistry {
    private final Map<String, SpeciesDefinition> byId = new LinkedHashMap<>();
    private final Map<String, SpeciesDefinition> byNormalizedNowns = new LinkedHashMap<>();

    public void clear() {
        byId.clear();
        byNormalizedNowns.clear();
    }

    public Iterable<SpeciesDefinition> all() {
        return byId.values();
    }

    public void register(SpeciesDefinition definition) {
        Objects.requireNonNull(definition);

        if (byId.containsKey(definition.id())) {
            throw new IllegalStateException("Duplicate molecule id: " + definition.id());
        }

        if (byNormalizedNowns.containsKey(definition.normalizedNowns())) {
            SpeciesDefinition existing = byNormalizedNowns.get(definition.normalizedNowns());
            throw new IllegalStateException(
                    "Duplicate normalized NOWNS '" + definition.normalizedNowns() +
                    "' for ids '" + existing.id() + "' and '" + definition.id() + "'"
            );
        }

        byId.put(definition.id(), definition);
        byNormalizedNowns.put(definition.normalizedNowns(), definition);
    }

    public SpeciesDefinition getById(String id) {
        return byId.get(id);
    }

    public SpeciesDefinition getByNormalizedNowns(String normalizedNowns) {
        return byNormalizedNowns.get(normalizedNowns);
    }

    public boolean containsId(String id) {
        return byId.containsKey(id);
    }

    public boolean containsNormalizedNowns(String normalizedNowns) {
        return byNormalizedNowns.containsKey(normalizedNowns);
    }

    public Collection<SpeciesDefinition> values() {
        return Collections.unmodifiableCollection(byId.values());
    }

    public Map<String, SpeciesDefinition> byIdView() {
        return Collections.unmodifiableMap(byId);
    }

    public Map<String, SpeciesDefinition> byNormalizedNownsView() {
        return Collections.unmodifiableMap(byNormalizedNowns);
    }

    public int size() {
        return byId.size();
    }
}