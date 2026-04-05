package com.penguin.nuclide.transport;

import com.penguin.nuclide.species.SpeciesStack;
import net.minecraft.util.math.Direction;

public interface SpeciesTransportNode {
    boolean canInsert(Direction side);

    boolean canExtract(Direction side);

    int insertSpecies(Direction side, SpeciesStack stack, boolean simulate);

    SpeciesStack extractSpecies(Direction side, SpeciesFilter filter, int maxAmount, boolean simulate);

    default boolean hasExtractableSpecies(Direction side, SpeciesFilter filter) {
        SpeciesStack extracted = extractSpecies(side, filter, 1, true);
        return extracted != null && !extracted.isEmpty();
    }
}