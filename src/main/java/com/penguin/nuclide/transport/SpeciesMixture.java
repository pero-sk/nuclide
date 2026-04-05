package com.penguin.nuclide.transport;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.penguin.nuclide.species.SpeciesStack;

public final class SpeciesMixture {
    private final Map<String, Integer> amounts = new HashMap<>();
    private final int capacity;

    public SpeciesMixture(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity cannot be negative");
        }
        this.capacity = capacity;
    }

    public int capacity() {
        return capacity;
    }

    public int totalAmount() {
        int total = 0;
        for (int amount : amounts.values()) {
            total += amount;
        }
        return total;
    }

    public int remainingCapacity() {
        return capacity - totalAmount();
    }

    public boolean isEmpty() {
        return amounts.isEmpty();
    }

    public Set<String> speciesIds() {
        return Collections.unmodifiableSet(amounts.keySet());
    }

    public int amountOf(String speciesId) {
        return amounts.getOrDefault(speciesId, 0);
    }

    public Map<String, Integer> asMap() {
        return Collections.unmodifiableMap(amounts);
    }

    public int insert(SpeciesStack stack, boolean simulate) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }

        int inserted = Math.min(stack.count(), remainingCapacity());
        if (inserted <= 0) {
            return 0;
        }

        if (!simulate) {
            amounts.merge(stack.speciesId(), inserted, Integer::sum);
        }

        return inserted;
    }

    public SpeciesStack extract(String speciesId, int maxAmount, boolean simulate) {
        if (speciesId == null || speciesId.isBlank()) {
            throw new IllegalArgumentException("speciesId cannot be blank");
        }

        if (maxAmount < 0) {
            throw new IllegalArgumentException("maxAmount cannot be negative");
        }

        int stored = amounts.getOrDefault(speciesId, 0);
        int extracted = Math.min(stored, maxAmount);

        if (extracted <= 0) {
            return null;
        }

        if (!simulate) {
            int remaining = stored - extracted;
            if (remaining == 0) {
                amounts.remove(speciesId);
            } else {
                amounts.put(speciesId, remaining);
            }
        }

        return new SpeciesStack(speciesId, extracted);
    }

    public SpeciesStack extractAny(SpeciesFilter filter, int maxAmount, boolean simulate) {
        SpeciesFilter actualFilter = filter != null ? filter : SpeciesFilter.any();

        for (Map.Entry<String, Integer> entry : amounts.entrySet()) {
            SpeciesStack candidate = new SpeciesStack(entry.getKey(), entry.getValue());
            if (actualFilter.test(candidate)) {
                return extract(entry.getKey(), maxAmount, simulate);
            }
        }

        return null;
    }
}