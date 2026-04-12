package com.penguin.nuclide.atomic;

import java.util.Objects;

public final class Bond {
    private final int a;
    private final int b;
    private final BondType type;

    public Bond(int a, int b, BondType type) {
        this.a = a;
        this.b = b;
        this.type = Objects.requireNonNull(type);
    }

    public int a() {
        return a;
    }

    public int b() {
        return b;
    }

    public BondType type() {
        return type;
    }

    @Override
    public String toString() {
        return "Bond{" +
                "a=" + a +
                ", b=" + b +
                ", type=" + type +
                '}';
    }
}