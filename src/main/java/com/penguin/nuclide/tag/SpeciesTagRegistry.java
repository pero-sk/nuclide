package com.penguin.nuclide.tag;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class SpeciesTagRegistry {
    private final Map<String, SpeciesTagDefinition> byId = new LinkedHashMap<>();

    public void clear() {
        byId.clear();
    }

    public SpeciesTagDefinition getOrCreate(String id) {
        Objects.requireNonNull(id);
        return byId.computeIfAbsent(id, SpeciesTagDefinition::new);
    }

    public SpeciesTagDefinition getById(String id) {
        return byId.get(id);
    }

    public boolean containsId(String id) {
        return byId.containsKey(id);
    }

    public int size() {
        return byId.size();
    }

    public Collection<SpeciesTagDefinition> values() {
        return Collections.unmodifiableCollection(byId.values());
    }

    public Map<String, SpeciesTagDefinition> byIdView() {
        return Collections.unmodifiableMap(byId);
    }
}