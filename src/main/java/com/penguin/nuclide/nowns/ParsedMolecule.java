package com.penguin.nuclide.nowns;

import java.util.Objects;

import com.penguin.nuclide.atomic.Molecule;

public final class ParsedMolecule {
    private final String namespace;
    private final Molecule molecule;

    public ParsedMolecule(String namespace, Molecule molecule) {
        this.namespace = Objects.requireNonNull(namespace);
        this.molecule = Objects.requireNonNull(molecule);
    }

    public String namespace() {
        return namespace;
    }

    public Molecule molecule() {
        return molecule;
    }

    @Override
    public String toString() {
        return "ParsedMolecule{namespace='" + namespace + "', molecule=" + molecule + "}";
    }
}