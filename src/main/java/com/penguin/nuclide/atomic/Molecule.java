package com.penguin.nuclide.atomic;

import java.util.ArrayList;
import java.util.List;

public final class Molecule {
    private final List<Atom> atoms = new ArrayList<>();
    private final List<Bond> bonds = new ArrayList<>();

    public List<Atom> atoms() {
        return atoms;
    }

    public List<Bond> bonds() {
        return bonds;
    }

    @Override
    public String toString() {
        return "Molecule{atoms=" + atoms + ", bonds=" + bonds + "}";
    }
}