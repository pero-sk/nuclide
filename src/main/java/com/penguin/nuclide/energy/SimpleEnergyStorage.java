package com.penguin.nuclide.energy;

public final class SimpleEnergyStorage {
    private final int capacity;
    private int stored;

    public SimpleEnergyStorage(int capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("capacity cannot be negative");
        }
        this.capacity = capacity;
    }

    public int getStored() {
        return stored;
    }

    public int getCapacity() {
        return capacity;
    }

    public int insert(int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }

        int accepted = Math.min(amount, capacity - stored);

        if (!simulate) {
            stored += accepted;
        }

        return accepted;
    }

    public int extract(int amount, boolean simulate) {
        if (amount <= 0) {
            return 0;
        }

        int extracted = Math.min(amount, stored);

        if (!simulate) {
            stored -= extracted;
        }

        return extracted;
    }

    public void setStored(int stored) {
        if (stored < 0 || stored > capacity) {
            throw new IllegalArgumentException("stored out of bounds");
        }
        this.stored = stored;
    }
}