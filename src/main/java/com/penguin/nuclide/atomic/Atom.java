package com.penguin.nuclide.atomic;

import java.util.Objects;

public final class Atom {
    private final String element;
    private final int charge;
    private final int isotope;
    private final boolean aromatic;

    public Atom(String element, int charge, int isotope, boolean aromatic) {
        this.element = Objects.requireNonNull(element);
        this.charge = charge;
        this.isotope = isotope;
        this.aromatic = aromatic;
    }

    public String element() {
        return element;
    }

    public int charge() {
        return charge;
    }

    public int isotope() {
        return isotope;
    }

    public boolean aromatic() {
        return aromatic;
    }

    @Override
    public String toString() {
        return "Atom{" +
                "element='" + element + '\'' +
                ", charge=" + charge +
                ", isotope=" + isotope +
                ", aromatic=" + aromatic +
                '}';
    }
}