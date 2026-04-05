package com.penguin.nuclide.data;

import com.penguin.nuclide.nowns.ParsedMolecule;
import com.penguin.nuclide.species.SpeciesRepresentation;
import com.penguin.nuclide.atomic.Molecule;
import com.penguin.nuclide.atomic.StateType;

import java.util.Objects;

public final class SpeciesDefinition {
    private final String id;
    private final String name;
    private final String rawNowns;
    private final String normalizedNowns;
    private final String namespace;
    private final Molecule molecule;
    private final ParsedMolecule parsed;

    // chemical properties
    private final StateType state;
    private final double meltingPoint;
    private final double boilingPoint;
    private final boolean radioactive;
    private final boolean toxic;
    private final boolean flammable;
    private final double molarMass;

    // kind
    private final SpeciesKind kind;
    private final SpeciesRepresentation representation;

    public SpeciesDefinition(
            String id,
            String name,
            String rawNowns,
            String normalizedNowns,
            String namespace,
            Molecule molecule,
            ParsedMolecule parsed,
            StateType state,
            double meltingPoint,
            double boilingPoint,
            boolean radioactive,
            boolean toxic,
            boolean flammable,
            double molarMass,
            SpeciesKind kind,
            SpeciesRepresentation representation
    ) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.rawNowns = Objects.requireNonNull(rawNowns);
        this.normalizedNowns = Objects.requireNonNull(normalizedNowns);
        this.namespace = Objects.requireNonNull(namespace);
        this.molecule = Objects.requireNonNull(molecule);
        this.parsed = Objects.requireNonNull(parsed);
        this.state = Objects.requireNonNull(state);
        this.meltingPoint = meltingPoint;
        this.boilingPoint = boilingPoint;
        this.radioactive = radioactive;
        this.toxic = toxic;
        this.flammable = flammable;
        this.molarMass = molarMass;
        this.kind = Objects.requireNonNull(kind);
        this.representation = Objects.requireNonNull(representation);
    }

    public String id() {
        return id;
    }

    public String name() {
        return name;
    }

    public String rawNowns() {
        return rawNowns;
    }

    public String normalizedNowns() {
        return normalizedNowns;
    }

    public String namespace() {
        return namespace;
    }

    public Molecule molecule() {
        return molecule;
    }

    public ParsedMolecule parsed() {
        return parsed;
    }

    public StateType state() {
        return state;
    }

    public double meltingPoint() {
        return meltingPoint;
    }

    public double boilingPoint() {
        return boilingPoint;
    }

    public boolean radioactive() {
        return radioactive;
    }

    public boolean toxic() {
        return toxic;
    }

    public boolean flammable() {
        return flammable;
    }

    public double molarMass() {
        return molarMass;
    }

    public SpeciesKind kind() {
        return kind;
    }

    public SpeciesRepresentation representation() {
        return representation;
    }

    @Override
    public String toString() {
        return "SpeciesDefinition{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", rawNowns='" + rawNowns + '\'' +
                ", normalizedNowns='" + normalizedNowns + '\'' +
                ", namespace='" + namespace + '\'' +
                '}';
    }
}