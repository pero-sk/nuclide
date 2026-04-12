package com.penguin.nuclide.transport;

import com.penguin.nuclide.species.SpeciesStack;

@FunctionalInterface
public interface SpeciesFilter {
    boolean test(SpeciesStack stack);

    static SpeciesFilter any() {
        return stack -> stack != null && !stack.isEmpty();
    }

    static SpeciesFilter species(String speciesId) {
        return stack -> stack != null && !stack.isEmpty() && stack.sameSpecies(speciesId);
    }
}