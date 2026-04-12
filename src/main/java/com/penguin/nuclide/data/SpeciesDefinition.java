package com.penguin.nuclide.data;

import com.penguin.nuclide.atomic.Molecule;
import com.penguin.nuclide.atomic.StateType;
import com.penguin.nuclide.nowns.ParsedMolecule;
import com.penguin.nuclide.species.SpeciesRepresentation;

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
    private final Double meltingPoint;
    private final Double boilingPoint;
    private final boolean radioactive;
    private final boolean toxic;
    private final boolean flammable;
    private final double molarMass;

    // kind
    private final SpeciesKind kind;
    private final SpeciesRepresentation representation;

    // stability
    private final StabilityDefinition stability;

    public SpeciesDefinition(
            String id,
            String name,
            String rawNowns,
            String normalizedNowns,
            String namespace,
            Molecule molecule,
            ParsedMolecule parsed,
            StateType state,
            Double meltingPoint,
            Double boilingPoint,
            boolean radioactive,
            boolean toxic,
            boolean flammable,
            double molarMass,
            SpeciesKind kind,
            SpeciesRepresentation representation,
            StabilityDefinition stability
    ) {
        this.id = Objects.requireNonNull(id, "id");
        this.name = Objects.requireNonNull(name, "name");
        this.rawNowns = Objects.requireNonNull(rawNowns, "rawNowns");
        this.normalizedNowns = Objects.requireNonNull(normalizedNowns, "normalizedNowns");
        this.namespace = Objects.requireNonNull(namespace, "namespace");
        this.molecule = Objects.requireNonNull(molecule, "molecule");
        this.parsed = Objects.requireNonNull(parsed, "parsed");
        this.state = Objects.requireNonNull(state, "state");
        this.meltingPoint = meltingPoint;
        this.boilingPoint = boilingPoint;
        this.radioactive = radioactive;
        this.toxic = toxic;
        this.flammable = flammable;
        this.molarMass = molarMass;
        this.kind = Objects.requireNonNull(kind, "kind");
        this.representation = Objects.requireNonNull(representation, "representation");
        this.stability = Objects.requireNonNull(stability, "stability");
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

    public Double meltingPoint() {
        return meltingPoint;
    }

    public Double boilingPoint() {
        return boilingPoint;
    }

    public boolean hasPhaseData() {
        return meltingPoint != null && boilingPoint != null;
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

    public StabilityDefinition stability() {
        return stability;
    }

    @Override
    public String toString() {
        return "SpeciesDefinition{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", rawNowns='" + rawNowns + '\'' +
                ", normalizedNowns='" + normalizedNowns + '\'' +
                ", namespace='" + namespace + '\'' +
                ", state=" + state +
                ", meltingPoint=" + meltingPoint +
                ", boilingPoint=" + boilingPoint +
                ", radioactive=" + radioactive +
                ", toxic=" + toxic +
                ", flammable=" + flammable +
                ", molarMass=" + molarMass +
                ", kind=" + kind +
                ", stability=" + stability +
                '}';
    }
}