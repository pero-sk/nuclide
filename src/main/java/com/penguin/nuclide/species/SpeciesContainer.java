package com.penguin.nuclide.species;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.mojang.serialization.Codec;
import com.penguin.nuclide.atomic.StateType;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;

public final class SpeciesContainer {
    private final Map<SpeciesKey, Integer> counts;

    public static final Codec<SpeciesContainer> CODEC =
            Codec.unboundedMap(SpeciesKey.CODEC, Codec.INT)
                    .xmap(SpeciesContainer::new, SpeciesContainer::asMap);

    public SpeciesContainer() {
        this.counts = new HashMap<>();
    }

    public SpeciesContainer(Map<SpeciesKey, Integer> initial) {
        Objects.requireNonNull(initial, "initial");
        this.counts = new HashMap<>();

        for (Map.Entry<SpeciesKey, Integer> entry : initial.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    public int countOf(SpeciesKey key) {
        Objects.requireNonNull(key, "key");
        return counts.getOrDefault(key, 0);
    }

    public int countOf(String speciesId, StateType currentState) {
        return countOf(new SpeciesKey(speciesId, currentState));
    }

    public boolean contains(SpeciesKey key, int amount) {
        Objects.requireNonNull(key, "key");
        if (amount < 0) {
            throw new IllegalArgumentException("amount cannot be negative");
        }
        return countOf(key) >= amount;
    }

    public boolean contains(String speciesId, StateType currentState, int amount) {
        return contains(new SpeciesKey(speciesId, currentState), amount);
    }

    public boolean isEmpty() {
        return counts.isEmpty();
    }

    public void add(SpeciesKey key, int amount) {
        Objects.requireNonNull(key, "key");
        if (amount < 0) {
            throw new IllegalArgumentException("amount cannot be negative");
        }
        if (amount == 0) {
            return;
        }

        int current = counts.getOrDefault(key, 0);
        int updated = Math.addExact(current, amount);
        counts.put(key, updated);
    }

    public void add(String speciesId, StateType currentState, int amount) {
        add(new SpeciesKey(speciesId, currentState), amount);
    }

    public void add(SpeciesStack stack) {
        Objects.requireNonNull(stack, "stack");
        add(stack.key(), stack.count());
    }

    public void remove(SpeciesKey key, int amount) {
        Objects.requireNonNull(key, "key");
        if (amount < 0) {
            throw new IllegalArgumentException("amount cannot be negative");
        }
        if (amount == 0) {
            return;
        }

        int current = counts.getOrDefault(key, 0);
        if (current < amount) {
            throw new IllegalArgumentException(
                    "Cannot remove " + amount + " of '" + key + "'; only " + current + " available"
            );
        }

        int updated = current - amount;
        if (updated == 0) {
            counts.remove(key);
        } else {
            counts.put(key, updated);
        }
    }

    public void remove(String speciesId, StateType currentState, int amount) {
        remove(new SpeciesKey(speciesId, currentState), amount);
    }

    public void remove(SpeciesStack stack) {
        Objects.requireNonNull(stack, "stack");
        remove(stack.key(), stack.count());
    }

    public List<SpeciesStack> stacks() {
        return counts.entrySet().stream()
                .map(e -> new SpeciesStack(e.getKey(), e.getValue()))
                .toList();
    }

    public SpeciesContainer copy() {
        return new SpeciesContainer(counts);
    }

    public Map<SpeciesKey, Integer> asMap() {
        return Map.copyOf(counts);
    }

    public void clear() {
        counts.clear();
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
            Map.Entry<SpeciesKey, Integer> largest = largestStackEntry();
            if (largest == null) {
                break;
            }

            SpeciesKey key = largest.getKey();
            int currentAmount = largest.getValue();

            int removed = Math.min(currentAmount, overflow);
            int updated = currentAmount - removed;

            if (updated == 0) {
                counts.remove(key);
            } else {
                counts.put(key, updated);
            }

            overflow -= removed;
            removedTotal += removed;
        }

        return removedTotal;
    }

    private Map.Entry<SpeciesKey, Integer> largestStackEntry() {
        Map.Entry<SpeciesKey, Integer> largest = null;

        for (Map.Entry<SpeciesKey, Integer> entry : counts.entrySet()) {
            if (largest == null || entry.getValue() > largest.getValue()) {
                largest = entry;
            }
        }

        return largest;
    }

    public int totalAmount() {
        return counts.values().stream().mapToInt(Integer::intValue).sum();
    }

    public NbtList toNbtList() {
        NbtList list = new NbtList();

        for (Map.Entry<SpeciesKey, Integer> entry : counts.entrySet()) {
            SpeciesKey key = entry.getKey();

            NbtCompound stackNbt = new NbtCompound();
            stackNbt.putString("Species", key.speciesId());
            stackNbt.putString("State", key.currentState().name());
            stackNbt.putInt("Count", entry.getValue());
            list.add(stackNbt);
        }

        return list;
    }

    public void fromNbtList(NbtList list) {
        counts.clear();

        for (int i = 0; i < list.size(); i++) {
            NbtCompound stackNbt = list.getCompound(i);

            String speciesId = stackNbt.getString("Species");
            String stateName = stackNbt.getString("State");
            int count = stackNbt.getInt("Count");

            if (speciesId.isBlank() || stateName.isBlank() || count <= 0) {
                continue;
            }

            StateType state;
            try {
                state = StateType.valueOf(stateName);
            } catch (IllegalArgumentException e) {
                continue;
            }

            counts.put(new SpeciesKey(speciesId, state), count);
        }
    }

    @Override
    public String toString() {
        return "SpeciesContainer{" +
                "counts=" + counts +
                '}';
    }
}