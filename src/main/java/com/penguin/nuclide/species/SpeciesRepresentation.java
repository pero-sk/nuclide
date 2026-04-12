package com.penguin.nuclide.species;

public record SpeciesRepresentation(
        PhaseRepresentation solid,
        PhaseRepresentation liquid,
        PhaseRepresentation gas
) {
    public static final SpeciesRepresentation EMPTY =
            new SpeciesRepresentation(null, null, null);

    public boolean hasSolid() {
        return solid != null && solid.block() != null;
    }

    public boolean hasLiquid() {
        return liquid != null && liquid.block() != null;
    }

    public boolean hasGas() {
        return gas != null && gas.block() != null;
    }
}