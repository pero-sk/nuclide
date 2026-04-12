package com.penguin.nuclide.reaction;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ReactionRegistry {
    private final Map<String, ReactionDefinition> byId = new LinkedHashMap<>();

    public void clear() {
        byId.clear();
    }

    public void register(ReactionDefinition definition) {
        Objects.requireNonNull(definition);

        if (byId.containsKey(definition.id())) {
            throw new IllegalStateException("Duplicate reaction id: " + definition.id());
        }

        byId.put(definition.id(), definition);
    }

    public ReactionDefinition getById(String id) {
        return byId.get(id);
    }

    public boolean containsId(String id) {
        return byId.containsKey(id);
    }

    public int size() {
        return byId.size();
    }

    public Collection<ReactionDefinition> values() {
        return Collections.unmodifiableCollection(byId.values());
    }

}