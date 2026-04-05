package com.penguin.nuclide.species;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.mojang.serialization.Codec;

public final class SpeciesContainer {
    private final Map<String, Integer> counts;

    public static final Codec<SpeciesContainer> CODEC =
            Codec.unboundedMap(Codec.STRING, Codec.INT)
                    .xmap(SpeciesContainer::new, SpeciesContainer::asMap);

    public SpeciesContainer() {
        this.counts = new HashMap<>();
    }

    public SpeciesContainer(Map<String, Integer> initial) {
        Objects.requireNonNull(initial, "initial");
        this.counts = new HashMap<>();
        for (Map.Entry<String, Integer> entry : initial.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    public int countOf(String speciesId) {
        validateSpeciesId(speciesId);
        return counts.getOrDefault(speciesId, 0);
    }

    public boolean contains(String speciesId, int amount) {
        if (amount < 0) throw new IllegalArgumentException("amount cannot be negative");
        return countOf(speciesId) >= amount;
    }

    public boolean isEmpty() {
        return counts.isEmpty();
    }

    public void add(String speciesId, int amount) {
        validateSpeciesId(speciesId);
        if (amount < 0) throw new IllegalArgumentException("amount cannot be negative");
        if (amount == 0) return;

        int current = counts.getOrDefault(speciesId, 0);
        int updated = Math.addExact(current, amount);
        counts.put(speciesId, updated);
    }

    public void add(SpeciesStack stack) {
        Objects.requireNonNull(stack, "stack");
        add(stack.speciesId(), stack.count());
    }

    public void remove(String speciesId, int amount) {
        validateSpeciesId(speciesId);
        if (amount < 0) throw new IllegalArgumentException("amount cannot be negative");
        if (amount == 0) return;

        int current = counts.getOrDefault(speciesId, 0);
        if (current < amount) {
            throw new IllegalArgumentException(
                    "Cannot remove " + amount + " of '" + speciesId + "'; only " + current + " available"
            );
        }

        int updated = current - amount;
        if (updated == 0) {
            counts.remove(speciesId);
        } else {
            counts.put(speciesId, updated);
        }
    }

    public void remove(SpeciesStack stack) {
        Objects.requireNonNull(stack, "stack");
        remove(stack.speciesId(), stack.count());
    }

    public List<SpeciesStack> stacks() {
        return counts.entrySet().stream()
                .map(e -> new SpeciesStack(e.getKey(), e.getValue()))
                .toList();
    }

    public SpeciesContainer copy() {
        return new SpeciesContainer(counts);
    }

    public Map<String, Integer> asMap() {
        return Map.copyOf(counts);
    }

public int clampToCapacity(int capacity) {
    if (capacity < 0) {
        throw new IllegalArgumentException("capacity cannot be negative");
    }

    int total = totalAmount();
    if (total <= capacity) {
        return 0;
    }

    int overflow = total - capacity;
    int removedTotal = 0;

    while (overflow > 0 && !counts.isEmpty()) {
        Map.Entry<String, Integer> largest = largestStackEntry();
        if (largest == null) {
            break;
        }

        String speciesId = largest.getKey();
        int currentAmount = largest.getValue();

        int removed = Math.min(currentAmount, overflow);
        int updated = currentAmount - removed;

        if (updated == 0) {
            counts.remove(speciesId);
        } else {
            counts.put(speciesId, updated);
        }

        overflow -= removed;
        removedTotal += removed;
    }

    return removedTotal;
}

    private Map.Entry<String, Integer> largestStackEntry() {
        Map.Entry<String, Integer> largest = null;

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            if (largest == null || entry.getValue() > largest.getValue()) {
                largest = entry;
            }
        }

        return largest;
    }

    public int totalAmount() {
        return counts.values().stream().mapToInt(Integer::intValue).sum();
    }

    private static void validateSpeciesId(String speciesId) {
        Objects.requireNonNull(speciesId, "speciesId");
        if (speciesId.isBlank()) {
            throw new IllegalArgumentException("speciesId cannot be blank");
        }
    }
    

    @Override
    public String toString() {
        return "SpeciesContainer{" +
                "counts=" + counts +
                '}';
    }
}