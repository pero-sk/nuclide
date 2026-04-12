package com.penguin.nuclide.renderer;

import java.util.ArrayList;
import java.util.List;

public final class MoleculeLayout {

    private final List<LayoutAtom> atoms = new ArrayList<>();
    private final List<LayoutBond> bonds = new ArrayList<>();

    public List<LayoutAtom> atoms() {
        return atoms;
    }

    public List<LayoutBond> bonds() {
        return bonds;
    }

    public void addAtom(LayoutAtom atom) {
        atoms.add(atom);
    }

    public void addBond(LayoutBond bond) {
        bonds.add(bond);
    }

    public record LayoutAtom(
            int atomIndex,
            String symbol,
            float x,
            float y
    ) {}

    public record LayoutBond(
            int fromAtomIndex,
            int toAtomIndex,
            int order
    ) {}
}